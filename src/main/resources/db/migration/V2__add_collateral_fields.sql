ALTER TABLE loan_application 
ADD COLUMN collateral_type VARCHAR(255),
ADD COLUMN collateral_value DECIMAL(15, 2);
