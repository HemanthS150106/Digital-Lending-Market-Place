# Implementation Results

## Implemented Features
- [x] Spring Boot 3.x Scaffold with Java 21
- [x] Flyway Migrations with Trace + User Profile + Nominee schema
- [x] JPA Entities (UserProfile, NomineeRequest, LoanApplication, DecisionTrace, RuleTraceEntry)
- [x] Pluggable Rules Engine with Strategy Pattern & `RuleExecutionContext`
- [x] **ML Credit Risk Scorer** — 5-feature Logistic Regression with sigmoid activation, feature normalisation, and XAI feature-contribution breakdown
- [x] Multi-party Nominee Approval Workflow (cross-user guarantor flow)
- [x] User Authentication (Login/Register with password) & Profile Management
- [x] React / Vite Frontend Dashboard with Login, Profile Editor, Nominee Modal
- [x] Docker-compose setup (PostgreSQL, Adminer, Spring Boot App, Nginx Frontend)
- [x] Decision Trace persistence with JSONB input snapshots
- [x] REST API Layer with Swagger/OpenAPI docs

## ML Model Details
| Property | Value |
|----------|-------|
| Algorithm | Logistic Regression (Binary Classification) |
| Features | DTI ratio, LTV ratio, Tenure, Loan Amount, Monthly Income |
| Activation | Sigmoid (σ) |
| Output | Probability of Default (PD), Estimated Credit Score (FICO scale) |
| Threshold | PD > 55% → HIGH RISK |
| Explainability | Full feature-contribution breakdown in Decision Trace |

## Sample API Requests

### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/v1/users/register \
-H "Content-Type: application/json" \
-d '{
    "id": "alice",
    "password": "password",
    "name": "Alice Smith",
    "monthlyIncome": 8000,
    "propertyValue": 200000
}'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/v1/users/login \
-H "Content-Type: application/json" \
-d '{"id": "alice", "password": "password"}'
```

### 3. Submit Loan Application
```bash
curl -X POST http://localhost:8080/api/v1/loan-applications \
-H "Content-Type: application/json" \
-d '{
    "lendingApp": {"id": 1},
    "borrowerId": "alice",
    "amount": 5000.00,
    "tenureMonths": 12
}'
```

### 4. Fetch Decision Trail (with ML XAI breakdown)
```bash
curl -X GET http://localhost:8080/api/v1/loan-applications/1/decision-trail
```

### 5. Request a Nominee
```bash
curl -X POST http://localhost:8080/api/v1/loan-applications/1/nominee \
-H "Content-Type: application/json" \
-d '{"nomineeId": "charlie"}'
```

### 6. Accept Nominee Request
```bash
curl -X POST http://localhost:8080/api/v1/loan-applications/nominee-requests/1/accept
```

## Known Limitations / TODOs
- Model weights are hardcoded (would be loaded from a serialised PMML/ONNX artifact in production).
- No online learning / feedback loop from actual loan outcomes.
- Missing EMI Amortization Scheduler.
- Pagination for endpoints.
- Password hashing (currently plaintext — would use BCrypt in production).
