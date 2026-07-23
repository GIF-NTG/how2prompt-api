-- =====================================================================
-- PHASE 1 - File 8/9: System
-- audit_logs: theo timeline Tuan 4, can ghi log hanh dong admin tu Phase 1.
-- refresh_tokens: can cho US-1.4 (login/refresh) tu Tuan 1.
-- notifications -> Phase 3 (US-8.7). api_keys -> Phase 4 (US-9.7).
-- =====================================================================

-- audit_logs la BIGSERIAL append-only log (insert volume cao,
-- khong bi FK tu bang khac tro vao, khong can UUID)
CREATE TABLE audit_logs (
  id            BIGSERIAL PRIMARY KEY,
  user_id       UUID REFERENCES users(id),
  workspace_id  UUID REFERENCES workspaces(id),
  action        VARCHAR(60) NOT NULL,
  resource_type VARCHAR(40),
  resource_id   UUID,
  ip_address    VARCHAR(45),
  user_agent    VARCHAR(500),
  metadata      JSONB NOT NULL DEFAULT '{}',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id_created_at ON audit_logs (user_id, created_at);
CREATE INDEX idx_audit_logs_workspace_id_created_at ON audit_logs (workspace_id, created_at);
CREATE INDEX idx_audit_logs_resource_type_resource_id ON audit_logs (resource_type, resource_id);

CREATE TABLE refresh_tokens (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    UUID NOT NULL REFERENCES users(id),
  token_hash VARCHAR(255) UNIQUE NOT NULL,
  user_agent VARCHAR(500),
  ip_address VARCHAR(45),
  expires_at TIMESTAMP NOT NULL,
  revoked_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
