package com.example.lending.service;

import com.example.lending.domain.*;
import com.example.lending.repository.*;
import com.example.lending.rulesengine.EligibilityRule;
import com.example.lending.rulesengine.PricingRule;
import com.example.lending.rulesengine.trace.RuleExecutionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final DecisionTraceRepository decisionTraceRepository;
    private final LoanOfferRepository loanOfferRepository;
    private final LenderRepository lenderRepository;
    private final List<EligibilityRule> eligibilityRules;
    private final List<PricingRule> pricingRules;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            DecisionTraceRepository decisionTraceRepository,
            LoanOfferRepository loanOfferRepository,
            LenderRepository lenderRepository,
            List<EligibilityRule> eligibilityRules,
            List<PricingRule> pricingRules) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.decisionTraceRepository = decisionTraceRepository;
        this.loanOfferRepository = loanOfferRepository;
        this.lenderRepository = lenderRepository;
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
        for (EligibilityRule rule : eligibilityRules) {
            boolean passed = rule.evaluate(context);
            if (!passed) {
                eligible = false;
                break; // Fast fail or evaluate all depending on requirements, doing fast fail here
            }
        }

        if (eligible) {
            trace.setOverallOutcome("ELIGIBLE");
            application.setStatus("APPROVED");

            // 4. Run Pricing Rules (simulate finding a lender for scaffolding)
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
}
