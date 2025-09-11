-- Account Service Database Migration
-- Version: 1.0
-- Description: Create account and wallet related tables

-- Create wallets table
CREATE TABLE IF NOT EXISTS wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    wallet_number VARCHAR(20) UNIQUE NOT NULL,
    balance DECIMAL(19,4) DEFAULT 0.0000,
    currency VARCHAR(5) DEFAULT 'NGN',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_frozen BOOLEAN DEFAULT false,
    frozen_reason TEXT,
    frozen_at TIMESTAMP,
    frozen_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type VARCHAR(20) DEFAULT 'SAVINGS',
    balance DECIMAL(19,4) DEFAULT 0.0000,
    available_balance DECIMAL(19,4) DEFAULT 0.0000,
    hold_amount DECIMAL(19,4) DEFAULT 0.0000,
    currency VARCHAR(5) DEFAULT 'NGN',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    interest_rate DECIMAL(8,6) DEFAULT 0.000000,
    minimum_balance DECIMAL(19,4) DEFAULT 0.0000,
    maximum_balance DECIMAL(19,4),
    daily_limit DECIMAL(19,4),
    monthly_limit DECIMAL(19,4),
    is_frozen BOOLEAN DEFAULT false,
    frozen_reason TEXT,
    frozen_at TIMESTAMP,
    frozen_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create account_holds table for managing holds
CREATE TABLE IF NOT EXISTS account_holds (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    hold_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    reference VARCHAR(100),
    description TEXT,
    expires_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallets_wallet_number ON wallets(wallet_number);
CREATE INDEX IF NOT EXISTS idx_wallets_status ON wallets(status);
CREATE INDEX IF NOT EXISTS idx_wallets_currency ON wallets(currency);
CREATE INDEX IF NOT EXISTS idx_accounts_wallet_id ON accounts(wallet_id);
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_accounts_status ON accounts(status);
CREATE INDEX IF NOT EXISTS idx_accounts_currency ON accounts(currency);
CREATE INDEX IF NOT EXISTS idx_account_holds_account_id ON account_holds(account_id);
CREATE INDEX IF NOT EXISTS idx_account_holds_status ON account_holds(status);
CREATE INDEX IF NOT EXISTS idx_account_holds_type ON account_holds(hold_type);

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_wallets_updated_at BEFORE UPDATE ON wallets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_account_holds_updated_at BEFORE UPDATE ON account_holds
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();