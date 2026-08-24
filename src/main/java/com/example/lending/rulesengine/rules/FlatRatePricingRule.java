package com.example.lending.rulesengine.rules;

import com.example.lending.domain.LoanOffer;
import com.example.lending.rulesengine.PricingRule;
import com.example.lending.rulesengine.trace.RuleExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class FlatRatePricingRule implements PricingRule {

    private static final BigDecimal FLAT_INTEREST_RATE = new BigDecimal("12.5");

    @Override
    public Optional<LoanOffer> applyPricing(RuleExecutionContext context) {
        // Just a dummy lender ID for sample, in a real scenario we'd look up the lender associated with this rule
        Long lenderId = 1L; 
        
        String inputSnapshot = String.format("{\"baseRate\": %s, \"loanAmount\": %s}", FLAT_INTEREST_RATE, context.getLoanApplication().getAmount());
        
        context.appendTrace(getRuleName(), "PRICING", inputSnapshot, "APPLIED", "Applied flat interest rate of 12.5%.");

        LoanOffer offer = new LoanOffer();
        offer.setAmount(context.getLoanApplication().getAmount());
        offer.setInterestRate(FLAT_INTEREST_RATE);
        offer.setTenureMonths(context.getLoanApplication().getTenureMonths());
        offer.setStatus("OFFERED");
        // lender will be attached by the service layer or we can query it here, 
        // for scaffolding we'll set it in the service layer.
        
        return Optional.of(offer);
    }

    @Override
    public String getRuleName() {
        return "FlatRatePricingRule";
    }
}
