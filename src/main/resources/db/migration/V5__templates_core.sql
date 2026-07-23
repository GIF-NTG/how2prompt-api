-- =====================================================================
-- PHASE 1 - File 5/9: Templates (core) - Epic 2 + Epic 3
-- Cac cot Phase 2/3 (forked_from_*, fork_count, rejection_reason,
-- quality_score, changelog, upvote_count, downvote_count, view_count)
-- KHONG nam trong file nay - se ALTER them o Phase 2/3.
-- =====================================================================

-- templates.current_version_id se REFERENCES template_versions(id),
-- nhung template_versions.template_id lai REFERENCES templates(id)
-- => phu thuoc vong. Tao templates truoc (chua co FK current_version_id)
-- -> tao template_versions -> ALTER templates ADD CONSTRAINT sau cung.

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

  status                   VARCHAR(20) NOT NULL DEFAULT 'draft',

  current_version_id       UUID,
  usage_count              BIGINT NOT NULL DEFAULT 0,
  favorite_count           INT NOT NULL DEFAULT 0,

  featured_at              TIMESTAMP,
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
