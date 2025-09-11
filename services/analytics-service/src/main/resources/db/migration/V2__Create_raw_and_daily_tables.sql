-- Create tables for raw event ingestion and daily aggregates

-- raw_events table stores ingested events in JSONB for schema-flexible analytics
CREATE TABLE IF NOT EXISTS raw_events (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload JSONB,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_raw_events_source ON raw_events(source);
CREATE INDEX IF NOT EXISTS idx_raw_events_received_at ON raw_events(received_at);
CREATE INDEX IF NOT EXISTS idx_raw_events_event_type ON raw_events(event_type);

-- daily_aggregates table stores daily KPIs for quick dashboard reads
CREATE TABLE IF NOT EXISTS daily_aggregates (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    transactions_count BIGINT DEFAULT 0,
    transactions_volume DECIMAL(19,2) DEFAULT 0,
    active_users BIGINT DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_daily_aggregates_date ON daily_aggregates(date);


