package com.example.lending.service;

import com.example.lending.domain.*;
import com.example.lending.repository.*;
import com.example.lending.rulesengine.EligibilityRule;
import com.example.lending.rulesengine.PricingRule;
import com.example.lending.rulesengine.trace.RuleExecutionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final DecisionTraceRepository decisionTraceRepository;
    private final LoanOfferRepository loanOfferRepository;
    private final LenderRepository lenderRepository;
    private final UserProfileRepository userProfileRepository;
    private final NomineeRequestRepository nomineeRequestRepository;
    private final List<EligibilityRule> eligibilityRules;
    private final List<PricingRule> pricingRules;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            DecisionTraceRepository decisionTraceRepository,
            LoanOfferRepository loanOfferRepository,
            LenderRepository lenderRepository,
            UserProfileRepository userProfileRepository,
            NomineeRequestRepository nomineeRequestRepository,
            List<EligibilityRule> eligibilityRules,
            List<PricingRule> pricingRules) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.decisionTraceRepository = decisionTraceRepository;
        this.loanOfferRepository = loanOfferRepository;
        this.lenderRepository = lenderRepository;
        this.userProfileRepository = userProfileRepository;
        this.nomineeRequestRepository = nomineeRequestRepository;
        this.eligibilityRules = eligibilityRules;
        this.pricingRules = pricingRules;
    }

    public List<LoanApplication> getAllApplications() {
        return loanApplicationRepository.findAll();
    }

    public List<LoanApplication> getApplicationsByBorrowerId(String borrowerId) {
        return loanApplicationRepository.findByBorrowerId(borrowerId);
    }

    public Optional<DecisionTrace> getDecisionTrace(Long applicationId) {
        return decisionTraceRepository.findByLoanApplicationId(applicationId);
    }

    @Transactional
    public LoanApplication processLoanApplication(LoanApplication application) {
        // Hydrate application with borrower profile details on initial run
        if (application.getId() == null) {
            UserProfile borrower = userProfileRepository.findById(application.getBorrowerId())
                    .orElseThrow(() -> new RuntimeException("Borrower profile not found"));
            application.setMonthlyIncome(borrower.getMonthlyIncome());
            application.setCollateralValue(borrower.getPropertyValue());
        }

        // 1. Save Application
        application.setStatus("PENDING");
        application = loanApplicationRepository.save(application);

        // 2. Initialize Trace
        DecisionTrace trace = new DecisionTrace();
        trace.setLoanApplication(application);
        trace.setOverallOutcome("PENDING");

        RuleExecutionContext context = new RuleExecutionContext(application, trace);

        // 3. Run Eligibility Rules
        boolean eligible = true;
        try {
            for (EligibilityRule rule : eligibilityRules) {
                boolean passed = rule.evaluate(context);
                if (!passed) {
                    eligible = false;
                    break;
                }
            }
        } catch (com.example.lending.rulesengine.NomineeRequiredException e) {
            trace.setOverallOutcome("NOMINEE_REQUIRED");
            application.setStatus("NOMINEE_REQUIRED");
            decisionTraceRepository.save(trace);
            return loanApplicationRepository.save(application);
        } catch (com.example.lending.rulesengine.CollateralRequiredException e) {
            trace.setOverallOutcome("COLLATERAL_REQUIRED");
            application.setStatus("COLLATERAL_REQUIRED");
            decisionTraceRepository.save(trace);
            return loanApplicationRepository.save(application);
        }

        if (eligible) {
            trace.setOverallOutcome("ELIGIBLE");
            application.setStatus("APPROVED");

            // 4. Run Pricing Rules
            Optional<Lender> lenderOpt = lenderRepository.findById(1L); // stub
            if (lenderOpt.isPresent()) {
                Lender lender = lenderOpt.get();
                for (PricingRule rule : pricingRules) {
                    Optional<LoanOffer> offerOpt = rule.applyPricing(context);
                    if (offerOpt.isPresent()) {
                        LoanOffer offer = offerOpt.get();
                        offer.setLoanApplication(application);
                        offer.setLender(lender);
                        loanOfferRepository.save(offer);
                    }
                }
            }
        } else {
            trace.setOverallOutcome("NOT_ELIGIBLE");
            application.setStatus("REJECTED");
        }

        // 5. Save Trace (cascades to RuleTraceEntries)
        decisionTraceRepository.save(trace);
        
        // 6. Update application status
        return loanApplicationRepository.save(application);
    }
    
    @Transactional
    public LoanApplication submitCollateral(Long applicationId, String collateralType, BigDecimal collateralValue) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
            
        if (!"COLLATERAL_REQUIRED".equals(application.getStatus()) && !"NOMINEE_REQUIRED".equals(application.getStatus())) {
            throw new RuntimeException("Collateral is not required for this application status.");
        }
        
        application.setCollateralType(collateralType);
        application.setCollateralValue(collateralValue);
        application = loanApplicationRepository.save(application);
        
        decisionTraceRepository.findByLoanApplicationId(application.getId())
            .ifPresent(trace -> decisionTraceRepository.delete(trace));
            
        return processLoanApplication(application);
    }

    @Transactional
    public NomineeRequest requestNominee(Long applicationId, String nomineeId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        UserProfile nominee = userProfileRepository.findById(nomineeId)
                .orElseThrow(() -> new RuntimeException("Nominee not found"));

        application.setStatus("NOMINEE_PENDING");
        loanApplicationRepository.save(application);

        NomineeRequest req = new NomineeRequest();
        req.setLoanApplication(application);
        req.setNominee(nominee);
        req.setStatus("PENDING");
        return nomineeRequestRepository.save(req);
    }

    @Transactional
    public LoanApplication acceptNomineeRequest(Long requestId) {
        NomineeRequest req = nomineeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus("ACCEPTED");
        nomineeRequestRepository.save(req);

        LoanApplication application = req.getLoanApplication();
        UserProfile borrower = userProfileRepository.findById(application.getBorrowerId()).get();
        UserProfile nominee = req.getNominee();

        // Combine incomes and property for re-evaluation
        application.setMonthlyIncome(borrower.getMonthlyIncome().add(nominee.getMonthlyIncome()));
        application.setCollateralValue(borrower.getPropertyValue().add(nominee.getPropertyValue()));
        application.setCollateralType("Nominee Guarantee: " + nominee.getName());
        application = loanApplicationRepository.save(application);

        decisionTraceRepository.findByLoanApplicationId(application.getId())
                .ifPresent(trace -> decisionTraceRepository.delete(trace));

        return processLoanApplication(application);
    }
}
