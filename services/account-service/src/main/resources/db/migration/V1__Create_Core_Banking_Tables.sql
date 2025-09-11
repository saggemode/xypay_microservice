-- Core Banking Database Migration
-- This script creates all necessary tables for the core banking system

-- Create accounts table with enhanced core banking features
CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_name VARCHAR(100),
    currency VARCHAR(5) NOT NULL DEFAULT 'NGN',
    account_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ledger_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    hold_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    overdraft_limit DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    overdraft_used DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    minimum_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    interest_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    branch_id BIGINT,
    branch_name VARCHAR(100),
    account_holder_name VARCHAR(100),
    account_holder_email VARCHAR(100),
    account_holder_phone VARCHAR(20),
    enable_notifications BOOLEAN NOT NULL DEFAULT true,
    enable_sms_alerts BOOLEAN NOT NULL DEFAULT true,
    enable_email_alerts BOOLEAN NOT NULL DEFAULT true,
    last_transaction_date TIMESTAMP,
    last_interest_calculation_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for accounts table
CREATE INDEX IF NOT EXISTS idx_account_customer ON accounts(customer_id);
CREATE INDEX IF NOT EXISTS idx_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_account_status ON accounts(status);
CREATE INDEX IF NOT EXISTS idx_account_type ON accounts(account_type);
CREATE INDEX IF NOT EXISTS idx_account_branch ON accounts(branch_id);
CREATE INDEX IF NOT EXISTS idx_account_currency ON accounts(currency);
CREATE INDEX IF NOT EXISTS idx_account_created_at ON accounts(created_at);
CREATE INDEX IF NOT EXISTS idx_account_last_transaction ON accounts(last_transaction_date);

-- Create transactions table for comprehensive transaction tracking
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    balance_before DECIMAL(19,2) NOT NULL,
    balance_after DECIMAL(19,2) NOT NULL,
    description VARCHAR(200),
    reference VARCHAR(50) UNIQUE,
    beneficiary_account_number VARCHAR(20),
    beneficiary_name VARCHAR(100),
    beneficiary_bank VARCHAR(100),
    channel VARCHAR(20),
    device_id VARCHAR(100),
    ip_address VARCHAR(45),
    location VARCHAR(100),
    teller_id VARCHAR(50),
    branch_id VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    is_reversal BOOLEAN NOT NULL DEFAULT false,
    original_transaction_id BIGINT,
    reversal_reason VARCHAR(200),
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    value_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for transactions table
CREATE INDEX IF NOT EXISTS idx_transaction_account ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_type ON transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_transaction_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transaction_reference ON transactions(reference);
CREATE INDEX IF NOT EXISTS idx_transaction_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transaction_account_number ON transactions(account_number);
CREATE INDEX IF NOT EXISTS idx_transaction_beneficiary ON transactions(beneficiary_account_number);
CREATE INDEX IF NOT EXISTS idx_transaction_channel ON transactions(channel);
CREATE INDEX IF NOT EXISTS idx_transaction_teller ON transactions(teller_id);
CREATE INDEX IF NOT EXISTS idx_transaction_branch ON transactions(branch_id);
CREATE INDEX IF NOT EXISTS idx_transaction_reversal ON transactions(is_reversal);
CREATE INDEX IF NOT EXISTS idx_transaction_original ON transactions(original_transaction_id);

-- Create account_limits table for transaction limits and controls
CREATE TABLE IF NOT EXISTS account_limits (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    limit_type VARCHAR(50) NOT NULL,
    limit_amount DECIMAL(19,2) NOT NULL,
    used_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    limit_period VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true,
    effective_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for account_limits table
CREATE INDEX IF NOT EXISTS idx_account_limits_account ON account_limits(account_id);
CREATE INDEX IF NOT EXISTS idx_account_limits_type ON account_limits(limit_type);
CREATE INDEX IF NOT EXISTS idx_account_limits_account_number ON account_limits(account_number);
CREATE INDEX IF NOT EXISTS idx_account_limits_period ON account_limits(limit_period);
CREATE INDEX IF NOT EXISTS idx_account_limits_active ON account_limits(is_active);
CREATE INDEX IF NOT EXISTS idx_account_limits_effective ON account_limits(effective_date);
CREATE INDEX IF NOT EXISTS idx_account_limits_expiry ON account_limits(expiry_date);

-- Create audit_logs table for comprehensive audit trail
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(100),
    reason VARCHAR(200)
);

-- Create indexes for audit_logs table
CREATE INDEX IF NOT EXISTS idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_entity_id ON audit_logs(entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_changed_by ON audit_logs(changed_by);
CREATE INDEX IF NOT EXISTS idx_audit_changed_at ON audit_logs(changed_at);
CREATE INDEX IF NOT EXISTS idx_audit_session ON audit_logs(session_id);

-- Create account_fees table for fee tracking
CREATE TABLE IF NOT EXISTS account_fees (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    fee_type VARCHAR(50) NOT NULL,
    fee_amount DECIMAL(19,2) NOT NULL,
    fee_period VARCHAR(20),
    is_waived BOOLEAN NOT NULL DEFAULT false,
    waiver_reason VARCHAR(200),
    waived_by VARCHAR(100),
    waived_at TIMESTAMP,
    charged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for account_fees table
CREATE INDEX IF NOT EXISTS idx_account_fees_account ON account_fees(account_id);
CREATE INDEX IF NOT EXISTS idx_account_fees_type ON account_fees(fee_type);
CREATE INDEX IF NOT EXISTS idx_account_fees_account_number ON account_fees(account_number);
CREATE INDEX IF NOT EXISTS idx_account_fees_period ON account_fees(fee_period);
CREATE INDEX IF NOT EXISTS idx_account_fees_waived ON account_fees(is_waived);
CREATE INDEX IF NOT EXISTS idx_account_fees_charged_at ON account_fees(charged_at);

-- Create interest_calculations table for interest tracking
CREATE TABLE IF NOT EXISTS interest_calculations (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    calculation_date DATE NOT NULL,
    principal_amount DECIMAL(19,2) NOT NULL,
    interest_rate DECIMAL(5,4) NOT NULL,
    days_calculated INTEGER NOT NULL,
    interest_amount DECIMAL(19,2) NOT NULL,
    is_credited BOOLEAN NOT NULL DEFAULT false,
    credited_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for interest_calculations table
CREATE INDEX IF NOT EXISTS idx_interest_account ON interest_calculations(account_id);
CREATE INDEX IF NOT EXISTS idx_interest_account_number ON interest_calculations(account_number);
CREATE INDEX IF NOT EXISTS idx_interest_calculation_date ON interest_calculations(calculation_date);
CREATE INDEX IF NOT EXISTS idx_interest_credited ON interest_calculations(is_credited);
CREATE INDEX IF NOT EXISTS idx_interest_credited_at ON interest_calculations(credited_at);

-- Create account_statements table for statement generation
CREATE TABLE IF NOT EXISTS account_statements (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    statement_period_start DATE NOT NULL,
    statement_period_end DATE NOT NULL,
    opening_balance DECIMAL(19,2) NOT NULL,
    closing_balance DECIMAL(19,2) NOT NULL,
    total_debits DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    total_credits DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    transaction_count INTEGER NOT NULL DEFAULT 0,
    statement_type VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_by VARCHAR(100),
    file_path VARCHAR(500),
    is_delivered BOOLEAN NOT NULL DEFAULT false,
    delivered_at TIMESTAMP
);

-- Create indexes for account_statements table
CREATE INDEX IF NOT EXISTS idx_statement_account ON account_statements(account_id);
CREATE INDEX IF NOT EXISTS idx_statement_account_number ON account_statements(account_number);
CREATE INDEX IF NOT EXISTS idx_statement_period_start ON account_statements(statement_period_start);
CREATE INDEX IF NOT EXISTS idx_statement_period_end ON account_statements(statement_period_end);
CREATE INDEX IF NOT EXISTS idx_statement_type ON account_statements(statement_type);
CREATE INDEX IF NOT EXISTS idx_statement_generated_at ON account_statements(generated_at);
CREATE INDEX IF NOT EXISTS idx_statement_delivered ON account_statements(is_delivered);

-- Create foreign key constraints
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_account 
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;

ALTER TABLE account_limits ADD CONSTRAINT fk_account_limits_account 
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;

ALTER TABLE account_fees ADD CONSTRAINT fk_account_fees_account 
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;

ALTER TABLE interest_calculations ADD CONSTRAINT fk_interest_calculations_account 
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;

ALTER TABLE account_statements ADD CONSTRAINT fk_account_statements_account 
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;

-- Create triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_account_limits_updated_at BEFORE UPDATE ON account_limits
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default account limits configuration
INSERT INTO account_limits (account_id, account_number, limit_type, limit_amount, limit_period, is_active) 
VALUES 
    (0, 'DEFAULT', 'DAILY_DEBIT', 50000.00, 'DAILY', true),
    (0, 'DEFAULT', 'DAILY_CREDIT', 100000.00, 'DAILY', true),
    (0, 'DEFAULT', 'SINGLE_TRANSACTION', 25000.00, 'SINGLE', true),
    (0, 'DEFAULT', 'MONTHLY_DEBIT', 500000.00, 'MONTHLY', true),
    (0, 'DEFAULT', 'ATM_DAILY', 20000.00, 'DAILY', true),
    (0, 'DEFAULT', 'TRANSFER_DAILY', 100000.00, 'DAILY', true),
    (0, 'DEFAULT', 'WITHDRAWAL_DAILY', 50000.00, 'DAILY', true);

-- Create views for common queries
CREATE OR REPLACE VIEW account_summary AS
SELECT 
    a.id,
    a.customer_id,
    a.account_number,
    a.account_name,
    a.account_type,
    a.status,
    a.currency,
    a.ledger_balance,
    a.available_balance,
    a.hold_balance,
    a.overdraft_limit,
    a.overdraft_used,
    (a.overdraft_limit - a.overdraft_used) as overdraft_available,
    a.minimum_balance,
    a.interest_rate,
    a.branch_id,
    a.branch_name,
    a.created_at,
    a.last_transaction_date,
    CASE 
        WHEN a.ledger_balance < a.minimum_balance THEN 'BELOW_MINIMUM'
        WHEN a.overdraft_used > 0 THEN 'OVERDRAWN'
        WHEN a.last_transaction_date < CURRENT_DATE - INTERVAL '365 days' THEN 'DORMANT'
        ELSE 'NORMAL'
    END as account_health
FROM accounts a;

CREATE OR REPLACE VIEW transaction_summary AS
SELECT 
    t.account_id,
    t.account_number,
    t.transaction_type,
    COUNT(*) as transaction_count,
    SUM(t.amount) as total_amount,
    AVG(t.amount) as average_amount,
    MIN(t.amount) as min_amount,
    MAX(t.amount) as max_amount,
    DATE(t.transaction_date) as transaction_date
FROM transactions t
WHERE t.status = 'COMPLETED'
GROUP BY t.account_id, t.account_number, t.transaction_type, DATE(t.transaction_date);

-- Create stored procedures for common operations
CREATE OR REPLACE FUNCTION calculate_daily_interest(
    p_account_id BIGINT,
    p_calculation_date DATE
) RETURNS DECIMAL(19,2) AS $$
DECLARE
    v_principal DECIMAL(19,2);
    v_interest_rate DECIMAL(5,4);
    v_interest_amount DECIMAL(19,2);
BEGIN
    -- Get account details
    SELECT ledger_balance, interest_rate 
    INTO v_principal, v_interest_rate
    FROM accounts 
    WHERE id = p_account_id AND status = 'ACTIVE';
    
    -- Calculate interest (simple interest for 1 day)
    v_interest_amount := v_principal * v_interest_rate / 100 / 365;
    
    -- Insert interest calculation record
    INSERT INTO interest_calculations (
        account_id, account_number, calculation_date, 
        principal_amount, interest_rate, days_calculated, interest_amount
    ) VALUES (
        p_account_id, 
        (SELECT account_number FROM accounts WHERE id = p_account_id),
        p_calculation_date, 
        v_principal, v_interest_rate, 1, v_interest_amount
    );
    
    RETURN v_interest_amount;
END;
$$ LANGUAGE plpgsql;

-- Create function to reset daily limits
CREATE OR REPLACE FUNCTION reset_daily_limits() RETURNS VOID AS $$
BEGIN
    UPDATE account_limits 
    SET used_amount = 0, updated_at = CURRENT_TIMESTAMP
    WHERE limit_period = 'DAILY' AND is_active = true;
END;
$$ LANGUAGE plpgsql;

-- Create function to reset monthly limits
CREATE OR REPLACE FUNCTION reset_monthly_limits() RETURNS VOID AS $$
BEGIN
    UPDATE account_limits 
    SET used_amount = 0, updated_at = CURRENT_TIMESTAMP
    WHERE limit_period = 'MONTHLY' AND is_active = true;
END;
$$ LANGUAGE plpgsql;

-- Grant necessary permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO postgres;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO postgres;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO postgres;
