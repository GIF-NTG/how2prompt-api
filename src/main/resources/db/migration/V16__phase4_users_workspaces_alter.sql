-- =====================================================================
-- PHASE 4 - File 1/6: ALTER users + workspaces (US-9.5, US-9.1)
-- =====================================================================

ALTER TABLE users
  ADD COLUMN plan VARCHAR(20) NOT NULL DEFAULT 'free';

ALTER TABLE workspaces
  ADD COLUMN plan       VARCHAR(20) NOT NULL DEFAULT 'free',
  ADD COLUMN avatar_url VARCHAR(500);

-- Ghi chu: workspaces.type = 'team' va workspace_members.role
-- (owner/admin/editor/viewer) KHONG doi schema - cot da co tu Phase 1,
-- gio moi enforce RBAC that o service layer (US-9.2).
