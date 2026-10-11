-- ==============================================================================
-- PixFlow: Schema Migration V2: Webhook Deliveries & Outbound Events
-- ==============================================================================

CREATE TABLE IF NOT EXISTS webhook_deliveries (
    id UUID PRIMARY KEY,
    charge_id UUID NOT NULL REFERENCES charges(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    signature VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_code INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_webhook_charge_id ON webhook_deliveries (charge_id);
CREATE INDEX IF NOT EXISTS idx_webhook_created_at ON webhook_deliveries (created_at DESC);
