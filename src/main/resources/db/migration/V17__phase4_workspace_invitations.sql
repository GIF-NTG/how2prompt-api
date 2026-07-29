-- =====================================================================
-- PHASE 4 - File 2/6: Workspace Invitations (US-9.1, Gap 1 - phuong an a)
-- Cho phep admin moi mot email CHUA tung dang ky. Khi user dang ky
-- dung email do, service layer se tu dong join workspace tuong ung.
-- =====================================================================

CREATE TABLE workspace_invitations (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID NOT NULL REFERENCES workspaces(id),
  email        VARCHAR(255) NOT NULL,
  role         VARCHAR(20) NOT NULL DEFAULT 'viewer',
  invited_by   UUID NOT NULL REFERENCES users(id),
  token        VARCHAR(255) UNIQUE NOT NULL,
  status       VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending/accepted/expired/revoked
  expires_at   TIMESTAMP NOT NULL,
  accepted_at  TIMESTAMP,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workspace_invitations_workspace_id ON workspace_invitations (workspace_id);
CREATE INDEX idx_workspace_invitations_email ON workspace_invitations (email);

-- Chan moi trung lap con dang cho (khong chan lai neu loi moi cu da
-- accepted/expired/revoked - cho phep moi lai)
CREATE UNIQUE INDEX idx_workspace_invitations_pending_unique
  ON workspace_invitations (workspace_id, email) WHERE status = 'pending';
