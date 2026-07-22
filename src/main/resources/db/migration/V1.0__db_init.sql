-- =====================================================================
-- How2Prompt - Initial Schema (Phase 1 MVP + forward-compatible tables)
-- Fixed vs. original how2prompt.sql:
--   1) VARCHAR(36) -> UUID (native, gen_random_uuid())
--   2) JSON -> JSONB (indexable, faster ->>/@> queries)
--   3) templates.search_vector TEXT -> tsvector + GIN index + trigger
--   4) Added GIN indexes on i18n / dynamic JSONB columns
--   5) Added partial indexes for soft-delete (deleted_at IS NULL)
--   6) Added partial unique index: only one is_current version per template
--   7) audit_logs.id -> BIGSERIAL (append-only log table, no UUID needed)
-- =====================================================================
 
CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- fuzzy search
 
-- =====================================================================
-- 1. IDENTITY & TENANCY
-- =====================================================================
 
CREATE TABLE users (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email             VARCHAR(255) UNIQUE NOT NULL,
  password_hash     VARCHAR(255),
  full_name         VARCHAR(150),
  username          VARCHAR(50) UNIQUE,
  avatar_url        VARCHAR(500),
  bio               TEXT,
  locale            VARCHAR(10) NOT NULL DEFAULT 'en',
  timezone          VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
  plan              VARCHAR(20) NOT NULL DEFAULT 'free',
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
  plan        VARCHAR(20) NOT NULL DEFAULT 'free',
  avatar_url  VARCHAR(500),
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
 
-- =====================================================================
-- 2. AI CATALOG
-- =====================================================================
 
CREATE TABLE ai_models (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code           VARCHAR(60) UNIQUE NOT NULL,
  name           VARCHAR(100) NOT NULL,
  provider       VARCHAR(40) NOT NULL,
  model_type     VARCHAR(20) NOT NULL,
  description    TEXT,
  capabilities   JSONB NOT NULL DEFAULT '{}',
  default_config JSONB NOT NULL DEFAULT '{}',
  icon_url       VARCHAR(500),
  doc_url        VARCHAR(500),
  is_active      BOOLEAN NOT NULL DEFAULT true,
  sort_order     INT NOT NULL DEFAULT 0,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- =====================================================================
-- 3. TAXONOMY
-- =====================================================================
 
CREATE TABLE categories (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  slug             VARCHAR(60) UNIQUE NOT NULL,
  name_i18n        JSONB NOT NULL,
  description_i18n JSONB NOT NULL DEFAULT '{}',
  icon             VARCHAR(60),
  color            VARCHAR(20),
  parent_id        UUID REFERENCES categories(id),
  sort_order       INT NOT NULL DEFAULT 0,
  is_active        BOOLEAN NOT NULL DEFAULT true,
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
CREATE INDEX idx_categories_parent_id ON categories (parent_id);
CREATE INDEX idx_categories_name_i18n ON categories USING GIN (name_i18n);
 
CREATE TABLE tags (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  slug        VARCHAR(60) UNIQUE NOT NULL,
  name        VARCHAR(80) NOT NULL,
  usage_count INT NOT NULL DEFAULT 0,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
CREATE INDEX idx_tags_name_trgm ON tags USING GIN (name gin_trgm_ops);
 
-- =====================================================================
-- 4. TEMPLATES (CORE)
-- =====================================================================
 
CREATE TABLE templates (
  id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id             UUID NOT NULL REFERENCES workspaces(id),
  slug                     VARCHAR(80) NOT NULL,
  title_i18n               JSONB NOT NULL,
  description_i18n         JSONB NOT NULL DEFAULT '{}',
  cover_image              VARCHAR(500),
 
  author_id                UUID REFERENCES users(id),
  author_type              VARCHAR(20) NOT NULL DEFAULT 'admin',
  is_official              BOOLEAN NOT NULL DEFAULT false,
  is_public                BOOLEAN NOT NULL DEFAULT false,
 
  forked_from_template_id  UUID REFERENCES templates(id),
  forked_from_version_id   UUID,
 
  status                   VARCHAR(20) NOT NULL DEFAULT 'draft',
  rejection_reason         TEXT,
 
  current_version_id       UUID,
  usage_count              BIGINT NOT NULL DEFAULT 0,
  fork_count               INT NOT NULL DEFAULT 0,
  upvote_count             INT NOT NULL DEFAULT 0,
  downvote_count           INT NOT NULL DEFAULT 0,
  favorite_count           INT NOT NULL DEFAULT 0,
  view_count               BIGINT NOT NULL DEFAULT 0,
 
  featured_at              TIMESTAMP,
  quality_score            NUMERIC(4,2),
  search_vector            tsvector,
 
  created_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_at             TIMESTAMP,
  deleted_at               TIMESTAMP,
 
  UNIQUE (workspace_id, slug)
);
 
CREATE INDEX idx_templates_status_is_public ON templates (status, is_public);
CREATE INDEX idx_templates_workspace_id ON templates (workspace_id);
CREATE INDEX idx_templates_author_id ON templates (author_id);
CREATE INDEX idx_templates_official_public_published ON templates (is_official, is_public, published_at);
CREATE INDEX idx_templates_forked_from_template_id ON templates (forked_from_template_id);
CREATE INDEX idx_templates_deleted_at ON templates (deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_templates_title_i18n ON templates USING GIN (title_i18n);
CREATE INDEX idx_templates_search_vector ON templates USING GIN (search_vector);
 
CREATE TABLE template_versions (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  template_id    UUID NOT NULL REFERENCES templates(id),
  version_number INT NOT NULL,
 
  prompt_body    TEXT NOT NULL,
  system_prompt  TEXT,
  example_output TEXT,
  guide_i18n     JSONB NOT NULL DEFAULT '{}',
  changelog      TEXT,
  is_current     BOOLEAN NOT NULL DEFAULT false,
 
  created_by     UUID REFERENCES users(id),
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 
  UNIQUE (template_id, version_number)
);
 
CREATE INDEX idx_template_versions_template_id ON template_versions (template_id);
-- Guarantees only one "current" version per template at DB level
CREATE UNIQUE INDEX idx_template_versions_one_current
  ON template_versions (template_id) WHERE is_current = true;
 
ALTER TABLE templates
  ADD CONSTRAINT fk_templates_forked_from_version_id
    FOREIGN KEY (forked_from_version_id) REFERENCES template_versions(id),
  ADD CONSTRAINT fk_templates_current_version_id
    FOREIGN KEY (current_version_id) REFERENCES template_versions(id);
 
CREATE TABLE template_variables (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  template_version_id UUID NOT NULL REFERENCES template_versions(id),
 
  var_key             VARCHAR(60) NOT NULL,
  label_i18n          JSONB NOT NULL,
  description_i18n    JSONB NOT NULL DEFAULT '{}',
  placeholder_i18n    JSONB NOT NULL DEFAULT '{}',
  help_text_i18n      JSONB NOT NULL DEFAULT '{}',
 
  input_type          VARCHAR(20) NOT NULL,
  is_required         BOOLEAN NOT NULL DEFAULT false,
  default_value       TEXT,
  options              JSONB NOT NULL DEFAULT '[]',
  validation           JSONB NOT NULL DEFAULT '{}',
 
  sort_order          INT NOT NULL DEFAULT 0,
  created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 
  UNIQUE (template_version_id, var_key)
);
 
CREATE INDEX idx_template_variables_template_version_id ON template_variables (template_version_id);
 
CREATE TABLE template_variants (
  id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  template_version_id    UUID NOT NULL REFERENCES template_versions(id),
  ai_model_id            UUID NOT NULL REFERENCES ai_models(id),
 
  prompt_body_override   TEXT,
  system_prompt_override TEXT,
  model_config           JSONB NOT NULL DEFAULT '{}',
  notes_i18n             JSONB NOT NULL DEFAULT '{}',
 
  created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 
  UNIQUE (template_version_id, ai_model_id)
);
 
CREATE TABLE template_categories (
  template_id UUID NOT NULL REFERENCES templates(id),
  category_id UUID NOT NULL REFERENCES categories(id),
  PRIMARY KEY (template_id, category_id)
);
 
CREATE TABLE template_tags (
  template_id UUID NOT NULL REFERENCES templates(id),
  tag_id      UUID NOT NULL REFERENCES tags(id),
  PRIMARY KEY (template_id, tag_id)
);
 
CREATE INDEX idx_template_tags_tag_id ON template_tags (tag_id);
 
CREATE TABLE template_models (
  template_id UUID NOT NULL REFERENCES templates(id),
  ai_model_id UUID NOT NULL REFERENCES ai_models(id),
  is_primary  BOOLEAN NOT NULL DEFAULT false,
  PRIMARY KEY (template_id, ai_model_id)
);
 
-- =====================================================================
-- 5. GENERATED PROMPTS
-- =====================================================================
 
CREATE TABLE generated_prompts (
  id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id                  UUID REFERENCES users(id),
  workspace_id             UUID NOT NULL REFERENCES workspaces(id),
 
  template_id              UUID REFERENCES templates(id),
  template_version_id      UUID REFERENCES template_versions(id),
  ai_model_id              UUID REFERENCES ai_models(id),
 
  title                    VARCHAR(200),
  input_values             JSONB NOT NULL DEFAULT '{}',
  extra_instructions       TEXT,
  final_prompt             TEXT NOT NULL,
 
  ai_score                 JSONB,
  ai_refined               TEXT,
  playground_response      TEXT,
  playground_response_meta JSONB,
 
  share_slug               VARCHAR(30) UNIQUE,
  is_public                BOOLEAN NOT NULL DEFAULT false,
 
  created_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at               TIMESTAMP
);
 
CREATE INDEX idx_generated_prompts_user_id_created_at ON generated_prompts (user_id, created_at);
CREATE INDEX idx_generated_prompts_template_id ON generated_prompts (template_id);
CREATE INDEX idx_generated_prompts_workspace_id ON generated_prompts (workspace_id);
CREATE INDEX idx_generated_prompts_is_public_created_at ON generated_prompts (is_public, created_at);
CREATE INDEX idx_generated_prompts_deleted_at ON generated_prompts (deleted_at) WHERE deleted_at IS NULL;
 
-- =====================================================================
-- 6. COMMUNITY (schema ready from Phase 1, features land Phase 3)
-- =====================================================================
 
CREATE TABLE favorites (
  user_id     UUID NOT NULL REFERENCES users(id),
  template_id UUID NOT NULL REFERENCES templates(id),
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, template_id)
);
 
CREATE INDEX idx_favorites_template_id ON favorites (template_id);
 
CREATE TABLE votes (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID NOT NULL REFERENCES users(id),
  target_type VARCHAR(30) NOT NULL,
  target_id   UUID NOT NULL,
  value       SMALLINT NOT NULL,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, target_type, target_id)
);
 
CREATE INDEX idx_votes_target_type_target_id ON votes (target_type, target_id);
 
CREATE TABLE comments (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID NOT NULL REFERENCES users(id),
  target_type  VARCHAR(30) NOT NULL,
  target_id    UUID NOT NULL,
  parent_id    UUID REFERENCES comments(id),
  content      TEXT NOT NULL,
  upvote_count INT NOT NULL DEFAULT 0,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at   TIMESTAMP
);
 
CREATE INDEX idx_comments_target_type_target_id_created_at ON comments (target_type, target_id, created_at);
CREATE INDEX idx_comments_parent_id ON comments (parent_id);
CREATE INDEX idx_comments_deleted_at ON comments (deleted_at) WHERE deleted_at IS NULL;
 
CREATE TABLE follows (
  follower_id UUID NOT NULL REFERENCES users(id),
  followee_id UUID NOT NULL REFERENCES users(id),
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (follower_id, followee_id)
);
 
CREATE INDEX idx_follows_followee_id ON follows (followee_id);
 
CREATE TABLE reports (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  reporter_id UUID NOT NULL REFERENCES users(id),
  target_type VARCHAR(30) NOT NULL,
  target_id   UUID NOT NULL,
  reason      VARCHAR(50) NOT NULL,
  detail      TEXT,
  status      VARCHAR(20) NOT NULL DEFAULT 'open',
  resolved_by UUID REFERENCES users(id),
  resolved_at TIMESTAMP,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
CREATE INDEX idx_reports_target_type_target_id ON reports (target_type, target_id);
CREATE INDEX idx_reports_status ON reports (status);
 
-- =====================================================================
-- 7. BILLING (Phase 4 - schema ready, not used in Phase 1)
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
 
-- =====================================================================
-- 8. SYSTEM
-- =====================================================================
 
-- audit_logs stays as a BIGSERIAL append-only log (high insert volume,
-- never referenced by FK from other tables, no need for UUID)
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
 
CREATE TABLE notifications (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    UUID NOT NULL REFERENCES users(id),
  type       VARCHAR(50) NOT NULL,
  title      VARCHAR(200) NOT NULL,
  body       TEXT,
  payload    JSONB NOT NULL DEFAULT '{}',
  read_at    TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
CREATE INDEX idx_notifications_user_id_created_at ON notifications (user_id, created_at);
 
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
 
-- =====================================================================
-- 9. TRIGGERS
-- =====================================================================
 
-- 9.1 Auto-update updated_at on row change (reused across tables)
CREATE OR REPLACE FUNCTION trg_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
 
CREATE TRIGGER set_updated_at_users BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_workspaces BEFORE UPDATE ON workspaces
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_templates BEFORE UPDATE ON templates
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_template_variants BEFORE UPDATE ON template_variants
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_generated_prompts BEFORE UPDATE ON generated_prompts
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_comments BEFORE UPDATE ON comments
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_ai_models BEFORE UPDATE ON ai_models
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_categories BEFORE UPDATE ON categories
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_subscriptions BEFORE UPDATE ON subscriptions
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
 
-- 9.2 Auto-update templates.search_vector from title_i18n + description_i18n
-- Concatenates both 'en' and 'vi' locale text so search works regardless
-- of the user's current UI language.
CREATE OR REPLACE FUNCTION trg_templates_search_vector()
RETURNS TRIGGER AS $$
BEGIN
  NEW.search_vector :=
    setweight(to_tsvector('simple', coalesce(NEW.title_i18n->>'en', '')), 'A') ||
    setweight(to_tsvector('simple', coalesce(NEW.title_i18n->>'vi', '')), 'A') ||
    setweight(to_tsvector('simple', coalesce(NEW.description_i18n->>'en', '')), 'B') ||
    setweight(to_tsvector('simple', coalesce(NEW.description_i18n->>'vi', '')), 'B');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
 
CREATE TRIGGER set_templates_search_vector
  BEFORE INSERT OR UPDATE OF title_i18n, description_i18n ON templates
  FOR EACH ROW EXECUTE FUNCTION trg_templates_search_vector();
 
-- Note on 'simple' text search config: chosen instead of 'english' because
-- content is mixed EN/VI. Postgres has no built-in Vietnamese dictionary,
-- so 'simple' (no stemming, just tokenizing + lowercasing) gives consistent
-- behavior across both languages. Revisit if VI search relevance is poor
-- (option: add unaccent extension + a custom VI dictionary later).