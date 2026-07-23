-- =====================================================================
-- PHASE 1 - File 3/9: AI Catalog (US-5.1)
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
