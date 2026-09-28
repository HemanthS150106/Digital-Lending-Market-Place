package com.example.lending.rulesengine.rules;

import com.example.lending.rulesengine.EligibilityRule;
import com.example.lending.rulesengine.trace.RuleExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

public class MinIncomeEligibilityRule implements EligibilityRule {

    private static final BigDecimal MIN_INCOME_THRESHOLD = new BigDecimal("2000.00");

    @Override
    public boolean evaluate(RuleExecutionContext context) {
        BigDecimal monthlyIncome = context.getLoanApplication().getMonthlyIncome();
        
        String inputSnapshot = String.format("{\"monthlyIncome\": %s, \"threshold\": %s}", monthlyIncome, MIN_INCOME_THRESHOLD);
        
        if (monthlyIncome.compareTo(MIN_INCOME_THRESHOLD) >= 0) {
            context.appendTrace(getRuleName(), "ELIGIBILITY", inputSnapshot, "PASS", "Monthly income meets the minimum threshold.");
            return true;
        } else {
            context.appendTrace(getRuleName(), "ELIGIBILITY", inputSnapshot, "FAIL", "Monthly income is below the minimum threshold.");
            return false;
        }
    }

    @Override
    public String getRuleName() {
        return "MinIncomeEligibilityRule";
    }
}
