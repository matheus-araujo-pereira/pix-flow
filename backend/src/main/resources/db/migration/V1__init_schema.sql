-- ==============================================================================
-- PixFlow: Event-Driven Payment & Settlement Engine
-- Schema Migration V1: Initial Schema
-- ==============================================================================

CREATE TABLE IF NOT EXISTS charges (
    id UUID PRIMARY KEY,
    tx_id VARCHAR(35) NOT NULL UNIQUE,
    end_to_end_id VARCHAR(32) NOT NULL UNIQUE,
    pix_key VARCHAR(140) NOT NULL,
    pix_key_type VARCHAR(20) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    qr_code_payload TEXT NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_charges_status ON charges (status);
CREATE INDEX IF NOT EXISTS idx_charges_created_at ON charges (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_charges_tx_id ON charges (tx_id);
CREATE INDEX IF NOT EXISTS idx_charges_end_to_end_id ON charges (end_to_end_id);

CREATE TABLE IF NOT EXISTS settlement_audit_logs (
    id UUID PRIMARY KEY,
    charge_id UUID NOT NULL REFERENCES charges(id) ON DELETE CASCADE,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    worker_id VARCHAR(100) NOT NULL,
    latency_ms BIGINT,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_charge_id ON settlement_audit_logs (charge_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON settlement_audit_logs (created_at DESC);

