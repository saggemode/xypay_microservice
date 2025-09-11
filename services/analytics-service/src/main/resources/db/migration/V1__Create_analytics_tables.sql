-- Analytics Service Database Migration
-- Version: 1.0
-- Description: Create analytics and ML related tables

-- Create customer_analytics table
CREATE TABLE IF NOT EXISTS customer_analytics (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    total_transaction_value DECIMAL(19,2) DEFAULT 0.00,
    average_account_balance DECIMAL(19,2) DEFAULT 0.00,
    credit_limit DECIMAL(19,2) DEFAULT 0.00,
    credit_used DECIMAL(19,2) DEFAULT 0.00,
    on_time_payments DECIMAL(10,0) DEFAULT 0,
    total_payments DECIMAL(10,0) DEFAULT 0,
    account_age_months BIGINT DEFAULT 0,
    pattern_consistency DECIMAL(5,4) DEFAULT 0.0000,
    risk_factor_count DECIMAL(10,0) DEFAULT 0,
    transaction_frequency DECIMAL(5,4) DEFAULT 0.0000,
    channel_diversity DECIMAL(5,4) DEFAULT 0.0000,
    product_usage DECIMAL(5,4) DEFAULT 0.0000,
    data_completeness DECIMAL(5,4) DEFAULT 0.0000,
    data_recency DECIMAL(5,4) DEFAULT 0.0000,
    last_updated TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create transaction_analytics table
CREATE TABLE IF NOT EXISTS transaction_analytics (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    amount DECIMAL(19,2),
    currency VARCHAR(3),
    transaction_type VARCHAR(20),
    channel VARCHAR(20),
    pattern_anomaly_score DECIMAL(5,4) DEFAULT 0.0000,
    transaction_velocity DECIMAL(5,2) DEFAULT 0.00,
    location_risk_score DECIMAL(5,4) DEFAULT 0.0000,
    time_risk_score DECIMAL(5,4) DEFAULT 0.0000,
    amount_risk_score DECIMAL(5,4) DEFAULT 0.0000,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create credit_scores table
CREATE TABLE IF NOT EXISTS credit_scores (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    score_date TIMESTAMP NOT NULL,
    risk_category VARCHAR(20),
    default_probability DECIMAL(5,4),
    is_active BOOLEAN DEFAULT true,
    model_version VARCHAR(20) DEFAULT '1.0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create credit_score_components table
CREATE TABLE IF NOT EXISTS credit_score_components (
    id BIGSERIAL PRIMARY KEY,
    credit_score_id BIGINT NOT NULL REFERENCES credit_scores(id) ON DELETE CASCADE,
    component_name VARCHAR(50) NOT NULL,
    component_value DECIMAL(5,2) NOT NULL
);

-- Create fraud_risk_scores table
CREATE TABLE IF NOT EXISTS fraud_risk_scores (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    risk_score DECIMAL(5,4) NOT NULL,
    assessment_date TIMESTAMP NOT NULL,
    risk_level VARCHAR(20),
    is_processed BOOLEAN DEFAULT false,
    processed_at TIMESTAMP,
    model_version VARCHAR(20) DEFAULT '1.0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create fraud_risk_factors table
CREATE TABLE IF NOT EXISTS fraud_risk_factors (
    id BIGSERIAL PRIMARY KEY,
    fraud_risk_score_id BIGINT NOT NULL REFERENCES fraud_risk_scores(id) ON DELETE CASCADE,
    factor_name VARCHAR(50) NOT NULL,
    factor_value DECIMAL(5,4) NOT NULL
);

-- Create fraud_recommendations table
CREATE TABLE IF NOT EXISTS fraud_recommendations (
    id BIGSERIAL PRIMARY KEY,
    fraud_risk_score_id BIGINT NOT NULL REFERENCES fraud_risk_scores(id) ON DELETE CASCADE,
    recommendation VARCHAR(200) NOT NULL
);

-- Create customer_segments table
CREATE TABLE IF NOT EXISTS customer_segments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    segment_type VARCHAR(20) NOT NULL,
    segment_date TIMESTAMP NOT NULL,
    customer_value DECIMAL(19,2),
    behavior_score DECIMAL(5,4),
    segment_confidence DECIMAL(5,4),
    is_active BOOLEAN DEFAULT true,
    model_version VARCHAR(20) DEFAULT '1.0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create risk_analytics table
CREATE TABLE IF NOT EXISTS risk_analytics (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT,
    transaction_id BIGINT,
    risk_type VARCHAR(30) NOT NULL,
    risk_score DECIMAL(5,4) NOT NULL,
    risk_level VARCHAR(20),
    assessment_date TIMESTAMP NOT NULL,
    risk_factors TEXT,
    mitigation_actions TEXT,
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    model_version VARCHAR(20) DEFAULT '1.0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    version BIGINT DEFAULT 0
);

-- Create ml_models table
CREATE TABLE IF NOT EXISTS ml_models (
    id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    version VARCHAR(20) NOT NULL,
    model_data TEXT,
    training_data_size BIGINT,
    accuracy DECIMAL(5,4),
    precision_score DECIMAL(5,4),
    recall_score DECIMAL(5,4),
    f1_score DECIMAL(5,4),
    training_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT false,
    deployment_date TIMESTAMP,
    performance_metrics TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_customer_analytics_customer_id ON customer_analytics(customer_id);
CREATE INDEX IF NOT EXISTS idx_transaction_analytics_transaction_id ON transaction_analytics(transaction_id);
CREATE INDEX IF NOT EXISTS idx_credit_scores_customer_id ON credit_scores(customer_id);
CREATE INDEX IF NOT EXISTS idx_credit_scores_active ON credit_scores(is_active);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_scores_transaction_id ON fraud_risk_scores(transaction_id);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_scores_processed ON fraud_risk_scores(is_processed);
CREATE INDEX IF NOT EXISTS idx_customer_segments_customer_id ON customer_segments(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_segments_active ON customer_segments(is_active);
CREATE INDEX IF NOT EXISTS idx_risk_analytics_customer_id ON risk_analytics(customer_id);
CREATE INDEX IF NOT EXISTS idx_risk_analytics_transaction_id ON risk_analytics(transaction_id);
CREATE INDEX IF NOT EXISTS idx_ml_models_type ON ml_models(model_type);
CREATE INDEX IF NOT EXISTS idx_ml_models_active ON ml_models(is_active);

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_customer_analytics_updated_at BEFORE UPDATE ON customer_analytics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transaction_analytics_updated_at BEFORE UPDATE ON transaction_analytics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_credit_scores_updated_at BEFORE UPDATE ON credit_scores
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_fraud_risk_scores_updated_at BEFORE UPDATE ON fraud_risk_scores
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customer_segments_updated_at BEFORE UPDATE ON customer_segments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_risk_analytics_updated_at BEFORE UPDATE ON risk_analytics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ml_models_updated_at BEFORE UPDATE ON ml_models
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
