-- Core entities
CREATE TABLE lending_app (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lender (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_service_provider (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_application (
    id SERIAL PRIMARY KEY,
    lending_app_id INT NOT NULL REFERENCES lending_app(id),
    borrower_id VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    tenure_months INT NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, APPROVED, REJECTED
    monthly_income DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_offer (
    id SERIAL PRIMARY KEY,
    loan_application_id INT NOT NULL REFERENCES loan_application(id),
    lender_id INT NOT NULL REFERENCES lender(id),
    amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    tenure_months INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Trace mechanism tables
CREATE TABLE decision_trace (
    id SERIAL PRIMARY KEY,
    loan_application_id INT NOT NULL REFERENCES loan_application(id),
    overall_outcome VARCHAR(50) NOT NULL, -- ELIGIBLE, NOT_ELIGIBLE
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rule_trace_entry (
    id SERIAL PRIMARY KEY,
    decision_trace_id INT NOT NULL REFERENCES decision_trace(id),
    rule_name VARCHAR(255) NOT NULL,
    rule_type VARCHAR(50) NOT NULL, -- ELIGIBILITY, PRICING
    input_snapshot JSONB,
    outcome VARCHAR(50) NOT NULL, -- PASS, FAIL, APPLIED
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert dummy data
INSERT INTO lending_app (name) VALUES ('FinApp One');
INSERT INTO lender (name) VALUES ('Bank of Tech');
INSERT INTO loan_service_provider (name) VALUES ('Credit Services Inc');
