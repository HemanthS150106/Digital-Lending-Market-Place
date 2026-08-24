package com.example.lending.rulesengine;

import com.example.lending.rulesengine.trace.RuleExecutionContext;

/**
 * Strategy interface for pluggable eligibility rules.
 */
public interface EligibilityRule {
    
    /**
     * @return true if the rule passes, false if it fails.
     */
    boolean evaluate(RuleExecutionContext context);
    
    /**
     * Standard rule name for tracing.
     */
    String getRuleName();
}
