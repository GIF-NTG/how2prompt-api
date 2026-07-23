-- =====================================================================
-- PHASE 1 - File 4/9: Taxonomy (US-5.2, US-2.1)
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
