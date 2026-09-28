# Interview Notes

### What problem does this solve?
It solves the problem of integrating fragmented credit systems (borrower apps, lenders, and service providers) into a single unified platform. More importantly, it solves the regulatory compliance challenge by making every automated credit decision fully explainable rather than a black-box output.

### What makes this different from a typical lending demo?
A standard demo usually has hardcoded "if/else" logic determining loan eligibility, returning only a simple boolean. This platform introduces three novel concepts:
1. **Explainability Layer (`DecisionTrace`)**: Automatically hooks into every evaluated rule, recording the rule's name, inputs, outcomes, and a human-readable reason into an immutable audit trail.
2. **Logistic Regression Credit Scorer**: Instead of a naive threshold check, I implemented a proper ML pipeline — feature extraction, feature engineering (DTI/LTV ratios), normalisation, weighted logit computation, sigmoid activation — producing a calibrated probability-of-default and an estimated credit score on the FICO scale. Every feature's individual contribution is captured for XAI compliance.
3. **Multi-Party Nominee Approvals**: If the ML model flags an application as High Risk, it halts the pipeline and triggers a **Nominee Flow**, allowing a secondary user (guarantor) to inject their financial profile. The system merges both profiles and re-runs the ML model automatically.

### Walk me through how the ML model works.
The model is a **5-feature Logistic Regression**:
1. **Feature Extraction**: Pull `monthly_income`, `loan_amount`, `collateral_value`, `tenure_months` from the application.
2. **Feature Engineering**: Derive `Debt-to-Income ratio` (loan / annual income) and `Loan-to-Value ratio` (loan / collateral).
3. **Normalisation**: Clamp features to `[0, 1]` using constants derived from the assumed training data distribution.
4. **Logit Calculation**: `z = bias + Σ(weight_i × normalised_feature_i)`. Positive weights (DTI, tenure, amount) increase default risk; negative weights (LTV, income) decrease it.
5. **Sigmoid Activation**: `P(default) = 1 / (1 + e^(-z))` — outputs a calibrated probability.
6. **Credit Score**: `score = (1 - PD) × 850` — maps probability to the familiar FICO scale.
7. **Decision**: If PD > 55%, the application is rejected or escalated to the Nominee Flow.

The key differentiator is that the Decision Trace records *every* intermediate value — raw features, normalised values, each weight's contribution, the logit, and the final PD — so an auditor can trace the rejection back to the exact feature that tipped the decision.

### Why Logistic Regression instead of a neural network?
Regulatory compliance (ECOA, FCRA) requires that credit decisions be explainable. Logistic Regression is inherently interpretable — each weight directly quantifies how much a feature influences the outcome. Neural networks are powerful but are "black boxes" that would require additional tools (LIME, SHAP) to explain, adding complexity without clear benefit for this use case.

### What was the hardest part to build and why?
Handling the state transitions for the Multi-Party Nominee flow combined with the ML Rules Engine. It's easy to build a linear approval pipeline, but allowing the pipeline to pause (`NOMINEE_REQUIRED`), wait for a secondary user to log in and asynchronously accept a request, then hydrate the application with the combined financial metrics of both users before re-evaluating the ML Model, required careful orchestration in the `LoanApplicationService` and deep integration with React state.

### What would you add with more time?
- **Online Learning**: Retrain model weights incrementally as new applications are processed, using a feedback loop from actual repayment outcomes.
- **EMI Amortization Scheduler**: Automatically computing the month-by-month repayment schedule for generated offers.
- **Rule Versioning**: Saving the model version when it ran (e.g., `CreditScorer_v2.1`) so historic traces remain valid even after model retraining.
- **SHAP Integration**: Adding Shapley values alongside weight contributions for even richer XAI explainability.
- **Async Offer Generation**: Generating offers asynchronously via Kafka/RabbitMQ since third-party lender APIs might be slow.
