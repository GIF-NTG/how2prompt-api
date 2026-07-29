-- =====================================================================
-- PHASE 4 - File 5/6: API Keys (US-9.7)
-- =====================================================================

CREATE TABLE api_keys (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID NOT NULL REFERENCES workspaces(id),
  user_id      UUID NOT NULL REFERENCES users(id),
  name         VARCHAR(100) NOT NULL,
  key_prefix   VARCHAR(12) NOT NULL,
  key_hash     VARCHAR(255) NOT NULL,
  scopes       JSONB NOT NULL DEFAULT '[]',
  last_used_at TIMESTAMP,
  expires_at   TIMESTAMP,
  revoked_at   TIMESTAMP,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_api_keys_workspace_id ON api_keys (workspace_id);
CREATE INDEX idx_api_keys_key_prefix ON api_keys (key_prefix);
