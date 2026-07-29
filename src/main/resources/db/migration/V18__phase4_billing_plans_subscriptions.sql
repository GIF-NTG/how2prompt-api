-- =====================================================================
-- PHASE 4 - File 3/6: Plans + Subscriptions (US-9.5)
-- =====================================================================

CREATE TABLE plans (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code           VARCHAR(30) UNIQUE NOT NULL,
  name           VARCHAR(60) NOT NULL,
  price_cents    INT NOT NULL DEFAULT 0,
  currency       VARCHAR(3) NOT NULL DEFAULT 'USD',
  billing_period VARCHAR(20) NOT NULL DEFAULT 'monthly',
  features       JSONB NOT NULL DEFAULT '{}',
  is_active      BOOLEAN NOT NULL DEFAULT true,
  sort_order     INT NOT NULL DEFAULT 0,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscriptions (
  id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id           UUID NOT NULL REFERENCES workspaces(id),
  plan_id                UUID NOT NULL REFERENCES plans(id),
  status                 VARCHAR(20) NOT NULL DEFAULT 'active',
  stripe_customer_id     VARCHAR(100),
  stripe_subscription_id VARCHAR(100),
  current_period_start   TIMESTAMP,
  current_period_end     TIMESTAMP,
  canceled_at            TIMESTAMP,
  trial_ends_at          TIMESTAMP,
  created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subscriptions_workspace_id ON subscriptions (workspace_id);

CREATE TRIGGER set_updated_at_subscriptions BEFORE UPDATE ON subscriptions
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
