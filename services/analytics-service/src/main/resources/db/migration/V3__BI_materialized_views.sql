-- Materialized views to support BI tools

CREATE MATERIALIZED VIEW IF NOT EXISTS mv_daily_kpis AS
SELECT da.date,
       da.transactions_count,
       da.transactions_volume,
       da.active_users
FROM daily_aggregates da
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS mv_transaction_breakdown AS
SELECT ta.transaction_type,
       ta.channel,
       COUNT(*) as tx_count,
       COALESCE(SUM(ta.amount), 0) as total_amount
FROM transaction_analytics ta
GROUP BY ta.transaction_type, ta.channel
WITH NO DATA;

-- Indexes for faster BI queries
CREATE INDEX IF NOT EXISTS idx_mv_daily_kpis_date ON mv_daily_kpis(date);


