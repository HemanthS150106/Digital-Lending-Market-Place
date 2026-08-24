package com.example.lending.rulesengine.trace;

import com.example.lending.domain.DecisionTrace;
import com.example.lending.domain.LoanApplication;
import com.example.lending.domain.RuleTraceEntry;

public class RuleExecutionContext {
    private final LoanApplication loanApplication;
    private final DecisionTrace decisionTrace;
    
    // We can also hold extra data or intermediate results here
    
    public RuleExecutionContext(LoanApplication loanApplication, DecisionTrace decisionTrace) {
        this.loanApplication = loanApplication;
        this.decisionTrace = decisionTrace;
    }

    public LoanApplication getLoanApplication() {
        return loanApplication;
    }

    public DecisionTrace getDecisionTrace() {
        return decisionTrace;
    }

    /**
     * Appends a trace entry from a rule evaluation.
     */
    public void appendTrace(String ruleName, String ruleType, String inputSnapshot, String outcome, String reason) {
        RuleTraceEntry entry = new RuleTraceEntry();
        entry.setRuleName(ruleName);
        entry.setRuleType(ruleType);
        entry.setInputSnapshot(inputSnapshot);
        entry.setOutcome(outcome);
        entry.setReason(reason);
        // Link to decision trace (which maintains the collection)
        this.decisionTrace.addTraceEntry(entry);
    }
}
