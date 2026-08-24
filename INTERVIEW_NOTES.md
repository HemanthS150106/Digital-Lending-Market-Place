# Interview Notes

### What problem does this solve?
It solves the problem of integrating fragmented credit systems (borrower apps, lenders, and service providers) into a single unified platform. More importantly, it solves the regulatory compliance challenge by making every automated credit decision fully explainable rather than a black-box output.

### What makes this different from a typical lending demo?
A standard demo usually has hardcoded "if/else" logic determining loan eligibility, returning only a simple boolean. This platform introduces a **novel explainability layer** (`RuleExecutionContext` and `DecisionTrace`) that automatically hooks into every evaluated rule, recording the rule's name, inputs, outcomes, and a human-readable reason into an immutable audit trail. This perfectly mirrors real-world credit bureau "Adverse Action" requirements.

### What was the hardest part to build and why?
Designing the Strategy pattern for the rules engine to enforce the trace recording. It's easy to build a pluggable engine, but ensuring that *every* external developer writing a new rule actually writes a trace entry required structuring the `RuleExecutionContext` to handle the complexity, forcing developers to interact with it to get their required data and append their outputs.

### What would you add with more time?
- **EMI Amortization Scheduler**: Automatically computing the month-by-month repayment schedule for generated offers.
- **Rule Versioning**: Saving the version of a rule when it ran (e.g., `MinIncomeEligibilityRule_v2`) so historic traces make sense even if the code changes.
- **Async Offer Generation**: Generating offers asynchronously via Kafka/RabbitMQ since third-party lender APIs might be slow.
- **Per-Lender Rate Limiting**: To prevent bad actors from spamming loan applications and exhausting a specific lender's API limits.
