-- Treasury Extended: accounts, statements, payments, approvals, reconciliations, fx_deals

CREATE TABLE IF NOT EXISTS bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    bank_name VARCHAR(100),
    account_name VARCHAR(150),
    account_number VARCHAR(50) UNIQUE,
    iban VARCHAR(34),
    swift_bic VARCHAR(11),
    currency_code VARCHAR(3) NOT NULL,
    type VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    kyc_metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS virtual_accounts (
    id BIGSERIAL PRIMARY KEY,
    bank_account_id BIGINT REFERENCES bank_accounts(id) ON DELETE CASCADE,
    virtual_iban VARCHAR(34) UNIQUE,
    reference_code VARCHAR(64) UNIQUE,
    customer_id BIGINT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bank_statements (
    id BIGSERIAL PRIMARY KEY,
    bank_account_id BIGINT REFERENCES bank_accounts(id) ON DELETE CASCADE,
    statement_date DATE NOT NULL,
    source VARCHAR(20) NOT NULL, -- MT940, CAMT.053, CSV
    file_name VARCHAR(255),
    raw_content TEXT,
    imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    debtor_account_id BIGINT REFERENCES bank_accounts(id),
    creditor_account VARCHAR(100),
    creditor_name VARCHAR(150),
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    scheme VARCHAR(20), -- ACH/SEPA/SWIFT/RTP
    purpose VARCHAR(140),
    status VARCHAR(20) DEFAULT 'PENDING',
    cut_off TIMESTAMP,
    scheduled_for TIMESTAMP,
    idempotency_key VARCHAR(80),
    UNIQUE (idempotency_key),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_approvals (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT REFERENCES payments(id) ON DELETE CASCADE,
    actor VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- MAKER/CHECKER
    decision VARCHAR(20) NOT NULL, -- APPROVE/REJECT
    reason VARCHAR(255),
    decided_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reconciliations (
    id BIGSERIAL PRIMARY KEY,
    statement_id BIGINT REFERENCES bank_statements(id) ON DELETE CASCADE,
    external_reference VARCHAR(100),
    internal_reference VARCHAR(100),
    status VARCHAR(20) DEFAULT 'UNMATCHED',
    tolerance DECIMAL(19,2) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fx_deals (
    id BIGSERIAL PRIMARY KEY,
    base_currency VARCHAR(3) NOT NULL,
    quote_currency VARCHAR(3) NOT NULL,
    notional DECIMAL(19,2) NOT NULL,
    rate DECIMAL(18,8) NOT NULL,
    type VARCHAR(10) NOT NULL, -- SPOT/FWD
    value_date DATE,
    pnl DECIMAL(19,2),
    status VARCHAR(20) DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_scheme ON payments(scheme);
CREATE INDEX IF NOT EXISTS idx_statements_account_date ON bank_statements(bank_account_id, statement_date);
CREATE INDEX IF NOT EXISTS idx_recon_status ON reconciliations(status);

-- Triggers
CREATE TRIGGER update_bank_accounts_updated_at BEFORE UPDATE ON bank_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_virtual_accounts_updated_at BEFORE UPDATE ON virtual_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_reconciliations_updated_at BEFORE UPDATE ON reconciliations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


