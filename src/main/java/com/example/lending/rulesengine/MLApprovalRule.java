package com.example.lending.rulesengine;

import com.example.lending.domain.DecisionTrace;
import com.example.lending.domain.LoanApplication;
import com.example.lending.domain.RuleTraceEntry;
import com.example.lending.rulesengine.trace.RuleExecutionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simulated ML Credit Risk Scoring Engine (v2).
 *
 * Implements a multi-feature Logistic Regression model that mirrors how
 * real-world credit scoring (e.g., FICO) works:
 *
 *   P(default) = sigmoid(w0 + w1*x1 + w2*x2 + ... + wN*xN)
 *
 * Features are extracted from the loan application and borrower profile,
 * normalised to [0,1], then fed through learned weights. The output is a
 * probability of default (PD). If PD exceeds the configured threshold the
 * application is flagged as HIGH RISK.
 *
 * Every intermediate value — raw features, normalised features, individual
 * weighted contributions, logit, and final probability — is captured in the
 * Decision Trace for full Explainable AI (XAI) compliance.
 */
@Component
public class MLApprovalRule implements EligibilityRule {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Model Hyperparameters (pretrained weights) ──────────────────────
    // In production these would come from a serialised model artifact (PMML/ONNX).
    // Here they are hard-coded to simulate a trained Logistic Regression.

    private static final double INTERCEPT = -2.5;   // bias term (w0)

    // Feature weights — positive = increases default risk
    private static final double W_DTI            =  3.8;   // Debt-to-Income ratio
    private static final double W_LTV            = -1.2;   // Loan-to-Value (collateral coverage)
    private static final double W_TENURE_RISK    =  1.5;   // longer tenure = more risk
    private static final double W_AMOUNT_BRACKET =  2.0;   // larger loans = more risk
    private static final double W_INCOME_STABILITY = -1.8;  // higher income = less risk

    // Normalisation constants (derived from "training data" distribution)
    private static final double MAX_DTI          = 5.0;
    private static final double MAX_LTV          = 3.0;
    private static final double MAX_TENURE       = 60.0;    // months
    private static final double MAX_AMOUNT       = 200_000; // dollars
    private static final double MAX_INCOME       = 20_000;  // dollars/month

    // Decision threshold
    private static final double PD_THRESHOLD = 0.55;  // P(default) > 55% → reject

    @Override
    public String getRuleName() {
        return "ML_LogisticRegression_CreditScorer_v2";
    }

    @Override
    public boolean evaluate(RuleExecutionContext context) {
        LoanApplication app = context.getLoanApplication();
        DecisionTrace trace = context.getDecisionTrace();

        // ── 1. Extract raw features ────────────────────────────────────
        double income     = app.getMonthlyIncome().doubleValue();
        double amount     = app.getAmount().doubleValue();
        double collateral = app.getCollateralValue() != null
                ? app.getCollateralValue().doubleValue() : 0.0;
        int    tenure     = app.getTenureMonths();

        // ── 2. Engineer derived features ───────────────────────────────
        double dti           = (income > 0) ? amount / (income * 12) : MAX_DTI;
        double ltv           = (collateral > 0) ? amount / collateral : MAX_LTV;
        double tenureRisk    = tenure;
        double amountBracket = amount;
        double incomeStability = income;

        // ── 3. Normalise to [0, 1] ─────────────────────────────────────
        double nDti       = clamp(dti / MAX_DTI);
        double nLtv       = clamp(ltv / MAX_LTV);
        double nTenure    = clamp(tenureRisk / MAX_TENURE);
        double nAmount    = clamp(amountBracket / MAX_AMOUNT);
        double nIncome    = clamp(incomeStability / MAX_INCOME);

        // ── 4. Compute logit (z = w·x + b) ────────────────────────────
        double contrib_dti    = W_DTI * nDti;
        double contrib_ltv    = W_LTV * nLtv;
        double contrib_tenure = W_TENURE_RISK * nTenure;
        double contrib_amount = W_AMOUNT_BRACKET * nAmount;
        double contrib_income = W_INCOME_STABILITY * nIncome;

        double logit = INTERCEPT
                + contrib_dti
                + contrib_ltv
                + contrib_tenure
                + contrib_amount
                + contrib_income;

        // ── 5. Sigmoid activation → probability of default ─────────────
        double probabilityOfDefault = 1.0 / (1.0 + Math.exp(-logit));
        int    creditScore          = (int) Math.round((1.0 - probabilityOfDefault) * 850);

        boolean passed = probabilityOfDefault <= PD_THRESHOLD;

        // ── 6. Build Explainable AI snapshot ───────────────────────────
        Map<String, Object> snapshot = new LinkedHashMap<>();

        snapshot.put("model", "LogisticRegression_CreditScorer_v2");
        snapshot.put("raw_features", Map.of(
                "monthly_income", income,
                "loan_amount", amount,
                "collateral_value", collateral,
                "tenure_months", tenure
        ));
        snapshot.put("engineered_features", Map.of(
                "debt_to_income_ratio", round(dti),
                "loan_to_value_ratio", round(ltv),
                "tenure_months", tenure,
                "amount_bracket", amount,
                "income_stability", income
        ));
        snapshot.put("normalised_features", Map.of(
                "n_dti", round(nDti),
                "n_ltv", round(nLtv),
                "n_tenure", round(nTenure),
                "n_amount", round(nAmount),
                "n_income", round(nIncome)
        ));

        // Feature contribution breakdown (key for XAI)
        Map<String, Object> contributions = new LinkedHashMap<>();
        contributions.put("intercept (bias)", round(INTERCEPT));
        contributions.put("DTI_contribution", round(contrib_dti));
        contributions.put("LTV_contribution", round(contrib_ltv));
        contributions.put("tenure_contribution", round(contrib_tenure));
        contributions.put("amount_contribution", round(contrib_amount));
        contributions.put("income_contribution", round(contrib_income));
        snapshot.put("feature_contributions", contributions);

        snapshot.put("logit_z", round(logit));
        snapshot.put("probability_of_default", round(probabilityOfDefault));
        snapshot.put("credit_score_estimate", creditScore);
        snapshot.put("threshold", PD_THRESHOLD);
        snapshot.put("verdict", passed ? "LOW_RISK" : "HIGH_RISK");

        // ── 7. Persist trace entry ─────────────────────────────────────
        RuleTraceEntry entry = new RuleTraceEntry();
        entry.setRuleName(getRuleName());
        entry.setRuleType("ELIGIBILITY");

        try {
            entry.setInputSnapshot(objectMapper.writeValueAsString(snapshot));
        } catch (JsonProcessingException e) {
            entry.setInputSnapshot("{}");
        }

        entry.setOutcome(passed ? "PASS" : "FAIL");
        entry.setReason(passed
                ? String.format("ML Model approved — PD=%.1f%% (threshold %.0f%%), estimated credit score %d",
                        probabilityOfDefault * 100, PD_THRESHOLD * 100, creditScore)
                : String.format("ML Model rejected — PD=%.1f%% exceeds threshold %.0f%%, estimated credit score %d",
                        probabilityOfDefault * 100, PD_THRESHOLD * 100, creditScore));
        trace.addTraceEntry(entry);

        // ── 8. Trigger Nominee flow if failed and no guarantor yet ─────
        if (!passed && (app.getCollateralType() == null || !app.getCollateralType().startsWith("Nominee Guarantee"))) {
            throw new NomineeRequiredException(
                    String.format("High Risk (PD=%.1f%%, Score=%d). Nominee guarantor required.",
                            probabilityOfDefault * 100, creditScore));
        }

        return passed;
    }

    // ── Utility helpers ────────────────────────────────────────────────
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
