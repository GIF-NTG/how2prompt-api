-- =====================================================================
-- PHASE 1 - File 2/9: Identity & Tenancy (Epic 1)
-- users.plan, users.username, workspaces.plan, workspaces.avatar_url
-- KHONG co o day - se them o Phase 4 (plan/avatar_url) va Phase 3 (username)
-- =====================================================================

CREATE TABLE users (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email             VARCHAR(255) UNIQUE NOT NULL,
  password_hash     VARCHAR(255),
  full_name         VARCHAR(150),
  avatar_url        VARCHAR(500),
  bio               TEXT,
  locale            VARCHAR(10) NOT NULL DEFAULT 'en',
  timezone          VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
  is_admin          BOOLEAN NOT NULL DEFAULT false,
  email_verified_at TIMESTAMP,
  last_login_at     TIMESTAMP,
  created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at        TIMESTAMP
);

CREATE INDEX idx_users_deleted_at ON users (deleted_at) WHERE deleted_at IS NULL;

CREATE TABLE user_identities (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID NOT NULL REFERENCES users(id),
  provider     VARCHAR(30) NOT NULL,
  provider_uid VARCHAR(255) NOT NULL,
  email        VARCHAR(255),
  raw_profile  JSONB,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (provider, provider_uid)
);

CREATE INDEX idx_user_identities_user_id ON user_identities (user_id);

CREATE TABLE workspaces (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  slug        VARCHAR(60) UNIQUE NOT NULL,
  name        VARCHAR(120) NOT NULL,
  type        VARCHAR(20) NOT NULL DEFAULT 'personal',
  owner_id    UUID NOT NULL REFERENCES users(id),
  settings    JSONB NOT NULL DEFAULT '{}',
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP
);

CREATE INDEX idx_workspaces_owner_id ON workspaces (owner_id);
CREATE INDEX idx_workspaces_deleted_at ON workspaces (deleted_at) WHERE deleted_at IS NULL;

CREATE TABLE workspace_members (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID NOT NULL REFERENCES workspaces(id),
  user_id      UUID NOT NULL REFERENCES users(id),
  role         VARCHAR(20) NOT NULL DEFAULT 'viewer',
  invited_by   UUID REFERENCES users(id),
  joined_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (workspace_id, user_id)
);

CREATE INDEX idx_workspace_members_user_id ON workspace_members (user_id);
