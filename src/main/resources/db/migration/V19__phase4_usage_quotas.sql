-- =====================================================================
-- PHASE 4 - File 4/6: Usage Quotas (US-9.6)
-- Luu y: co che nay KHONG thay the Redis rate-limit (infrastructure/
-- ratelimit) da co tu Phase 1. Redis rate limit = chong spam tuc thoi
-- (request/phut). usage_quotas (Postgres) = dem quota theo plan, theo
-- ky billing. Ca hai cung ton tai song song.
-- =====================================================================

CREATE TABLE usage_quotas (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID NOT NULL REFERENCES workspaces(id),
  feature      VARCHAR(50) NOT NULL,
  period_start DATE NOT NULL,
  period_end   DATE NOT NULL,
  used_count   INT NOT NULL DEFAULT 0,
  limit_count  INT NOT NULL,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (workspace_id, feature, period_start)
);

CREATE INDEX idx_usage_quotas_workspace_id_feature ON usage_quotas (workspace_id, feature);
