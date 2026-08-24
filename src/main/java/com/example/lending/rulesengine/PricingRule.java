package com.example.lending.rulesengine;

import com.example.lending.domain.LoanOffer;
import com.example.lending.rulesengine.trace.RuleExecutionContext;

import java.util.Optional;

/**
 * Strategy interface for pluggable pricing rules.
 */
public interface PricingRule {
    
    /**
     * Generates a loan offer if applicable, or returns empty.
     */
    Optional<LoanOffer> applyPricing(RuleExecutionContext context);
    
    /**
     * Standard rule name for tracing.
     */
    String getRuleName();
}
