CREATE TABLE user_profile (
    id VARCHAR(255) PRIMARY KEY, -- using username/borrower_id as ID
    password VARCHAR(255) NOT NULL DEFAULT 'password',
    name VARCHAR(255) NOT NULL,
    monthly_income DECIMAL(15, 2) NOT NULL DEFAULT 0,
    property_value DECIMAL(15, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE nominee_request (
    id BIGSERIAL PRIMARY KEY,
    loan_application_id BIGINT NOT NULL REFERENCES loan_application(id),
    nominee_id VARCHAR(255) NOT NULL REFERENCES user_profile(id),
    status VARCHAR(50) NOT NULL, -- PENDING, ACCEPTED, REJECTED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert demo users
INSERT INTO user_profile (id, password, name, monthly_income, property_value) VALUES 
('alice', 'password', 'Alice (High Income)', 8000, 200000),
('bob', 'password', 'Bob (High Debt)', 2000, 0),
('charlie', 'password', 'Charlie (Nominee/Rich)', 15000, 500000);
