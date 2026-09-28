package com.example.lending.rulesengine.rules;

import com.example.lending.rulesengine.CollateralRequiredException;
import com.example.lending.rulesengine.EligibilityRule;
import com.example.lending.rulesengine.trace.RuleExecutionContext;
import com.example.lending.service.CreditBureauService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

public class AffordabilityEligibilityRule implements EligibilityRule {

    private final CreditBureauService creditBureauService;

    public AffordabilityEligibilityRule(CreditBureauService creditBureauService) {
        this.creditBureauService = creditBureauService;
    }

    @Override
    public boolean evaluate(RuleExecutionContext context) {
        BigDecimal income = context.getLoanApplication().getMonthlyIncome();
        BigDecimal existingEmis = creditBureauService.getExistingMonthlyEmis(context.getLoanApplication().getBorrowerId());
        BigDecimal proposedEmi = EmiCalculator.calculateEmi(
                context.getLoanApplication().getAmount(), 
                context.getLoanApplication().getTenureMonths()
        );
        
        BigDecimal totalEmi = existingEmis.add(proposedEmi);
        // Max allowable DTI is 50%
        BigDecimal maxAllowableEmi = income.multiply(new BigDecimal("0.50"));
        
        String inputSnapshot = String.format("{\"income\": %s, \"existingEmis\": %s, \"proposedEmi\": %s, \"totalEmi\": %s, \"maxAllowableEmi\": %s}",
                income, existingEmis, proposedEmi, totalEmi, maxAllowableEmi);
                
        if (totalEmi.compareTo(maxAllowableEmi) <= 0) {
            context.appendTrace(getRuleName(), "ELIGIBILITY", inputSnapshot, "PASS", "Borrower can afford the proposed EMI.");
            return true;
        } else {
            // Cannot afford via income alone. Check collateral.
            BigDecimal collateralValue = context.getLoanApplication().getCollateralValue();
            BigDecimal loanAmount = context.getLoanApplication().getAmount();
            
            if (collateralValue != null && collateralValue.compareTo(loanAmount) >= 0) {
                String collateralSnapshot = String.format("{\"collateralType\": \"%s\", \"collateralValue\": %s, \"loanAmount\": %s}",
                        context.getLoanApplication().getCollateralType(), collateralValue, loanAmount);
                        
                context.appendTrace(getRuleName(), "ELIGIBILITY", collateralSnapshot, "PASS", "Income insufficient, but provided collateral covers the loan risk.");
                return true;
            } else {
                context.appendTrace(getRuleName(), "ELIGIBILITY", inputSnapshot, "FAIL", "Income insufficient to support total EMI, and no valid collateral provided.");
                throw new CollateralRequiredException("DTI > 50% and collateral missing or insufficient.");
            }
        }
    }

    @Override
    public String getRuleName() {
        return "AffordabilityEligibilityRule";
    }
}
