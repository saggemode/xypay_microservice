-- Transaction Service Database Migration
-- Version: 1.0
-- Description: Create transaction related tables

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    receiver_account_number VARCHAR(20),
    reference VARCHAR(50) UNIQUE NOT NULL,
    amount DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    type VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    balance_after DECIMAL(19,4),
    parent_id BIGINT,
    currency VARCHAR(5) NOT NULL DEFAULT 'NGN',
    metadata TEXT,
    idempotency_key VARCHAR(100) UNIQUE,
    direction VARCHAR(10) NOT NULL,
    processed_at TIMESTAMP,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create transaction_audit table for audit trail
CREATE TABLE IF NOT EXISTS transaction_audit (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(10),
    new_status VARCHAR(10),
    old_amount DECIMAL(19,4),
    new_amount DECIMAL(19,4),
    reason TEXT,
    performed_by VARCHAR(50),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- Create transaction_fees table
CREATE TABLE IF NOT EXISTS transaction_fees (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    fee_type VARCHAR(20) NOT NULL,
    fee_amount DECIMAL(19,4) NOT NULL,
    fee_percentage DECIMAL(5,4),
    currency VARCHAR(5) NOT NULL DEFAULT 'NGN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_transactions_account_number ON transactions(account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_receiver_account_number ON transactions(receiver_account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_reference ON transactions(reference);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_channel ON transactions(channel);
CREATE INDEX IF NOT EXISTS idx_transactions_currency ON transactions(currency);
CREATE INDEX IF NOT EXISTS idx_transactions_timestamp ON transactions(timestamp);
CREATE INDEX IF NOT EXISTS idx_transactions_idempotency_key ON transactions(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_transaction_audit_transaction_id ON transaction_audit(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_audit_performed_at ON transaction_audit(performed_at);
CREATE INDEX IF NOT EXISTS idx_transaction_fees_transaction_id ON transaction_fees(transaction_id);

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
