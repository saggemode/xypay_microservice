-- Treasury Service Database Migration
-- Version: 1.0
-- Description: Create treasury related tables

-- Create treasury_positions table
CREATE TABLE IF NOT EXISTS treasury_positions (
    id BIGSERIAL PRIMARY KEY,
    currency_code VARCHAR(3) NOT NULL,
    position_amount DECIMAL(19,2) NOT NULL,
    available_amount DECIMAL(19,2),
    reserved_amount DECIMAL(19,2),
    value_date DATE NOT NULL,
    maturity_date DATE,
    position_type VARCHAR(20),
    liquidity_bucket VARCHAR(20),
    interest_rate DECIMAL(8,6),
    cost_center VARCHAR(50),
    profit_center VARCHAR(50),
    risk_weight DECIMAL(5,2) DEFAULT 100.00,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create liquidity_forecasts table
CREATE TABLE IF NOT EXISTS liquidity_forecasts (
    id BIGSERIAL PRIMARY KEY,
    currency_code VARCHAR(3) NOT NULL,
    forecast_date DATE NOT NULL,
    forecast_days INTEGER NOT NULL,
    current_liquidity DECIMAL(19,2) NOT NULL,
    minimum_required_liquidity DECIMAL(19,2),
    risk_tolerance DECIMAL(5,2) DEFAULT 0.10,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create liquidity_projections table
CREATE TABLE IF NOT EXISTS liquidity_projections (
    id BIGSERIAL PRIMARY KEY,
    liquidity_forecast_id BIGINT NOT NULL REFERENCES liquidity_forecasts(id) ON DELETE CASCADE,
    forecast_date DATE NOT NULL,
    projected_liquidity DECIMAL(19,2) NOT NULL,
    confidence_level DECIMAL(5,4),
    variance DECIMAL(19,2),
    scenario VARCHAR(50) DEFAULT 'BASE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create risk_metrics table
CREATE TABLE IF NOT EXISTS risk_metrics (
    id BIGSERIAL PRIMARY KEY,
    liquidity_forecast_id BIGINT REFERENCES liquidity_forecasts(id),
    liquidity_risk DECIMAL(19,2),
    concentration_risk DECIMAL(19,2),
    market_risk DECIMAL(19,2),
    operational_risk DECIMAL(19,2),
    total_risk DECIMAL(19,2),
    var_95 DECIMAL(19,2),
    var_99 DECIMAL(19,2),
    expected_shortfall DECIMAL(19,2),
    risk_tolerance_breach BOOLEAN DEFAULT false,
    risk_rating VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create treasury_transactions table
CREATE TABLE IF NOT EXISTS treasury_transactions (
    id BIGSERIAL PRIMARY KEY,
    treasury_position_id BIGINT REFERENCES treasury_positions(id),
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    reference VARCHAR(50),
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    value_date TIMESTAMP NOT NULL,
    settlement_date TIMESTAMP,
    counterparty VARCHAR(100),
    external_reference VARCHAR(100),
    metadata TEXT,
    transaction_category VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_treasury_positions_currency ON treasury_positions(currency_code);
CREATE INDEX IF NOT EXISTS idx_treasury_positions_active ON treasury_positions(is_active);
CREATE INDEX IF NOT EXISTS idx_treasury_positions_type ON treasury_positions(position_type);
CREATE INDEX IF NOT EXISTS idx_treasury_positions_bucket ON treasury_positions(liquidity_bucket);
CREATE INDEX IF NOT EXISTS idx_liquidity_forecasts_currency ON liquidity_forecasts(currency_code);
CREATE INDEX IF NOT EXISTS idx_liquidity_forecasts_date ON liquidity_forecasts(forecast_date);
CREATE INDEX IF NOT EXISTS idx_liquidity_projections_forecast ON liquidity_projections(liquidity_forecast_id);
CREATE INDEX IF NOT EXISTS idx_liquidity_projections_date ON liquidity_projections(forecast_date);
CREATE INDEX IF NOT EXISTS idx_risk_metrics_forecast ON risk_metrics(liquidity_forecast_id);
CREATE INDEX IF NOT EXISTS idx_treasury_transactions_position ON treasury_transactions(treasury_position_id);
CREATE INDEX IF NOT EXISTS idx_treasury_transactions_type ON treasury_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_treasury_transactions_status ON treasury_transactions(status);

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_treasury_positions_updated_at BEFORE UPDATE ON treasury_positions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_liquidity_forecasts_updated_at BEFORE UPDATE ON liquidity_forecasts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_liquidity_projections_updated_at BEFORE UPDATE ON liquidity_projections
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_risk_metrics_updated_at BEFORE UPDATE ON risk_metrics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_treasury_transactions_updated_at BEFORE UPDATE ON treasury_transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
