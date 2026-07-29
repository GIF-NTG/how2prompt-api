-- =====================================================================
-- PHASE 2 - File 2/2: AI Enhancement (Epic 6)
-- Khong them translated_from_id (US-6.3) - da chot dung phuong an (b):
-- moi lan dich coi nhu 1 lan generate doc lap, khong can traceability.
-- =====================================================================

ALTER TABLE generated_prompts
  ADD COLUMN ai_score                 JSONB,
  ADD COLUMN ai_refined                TEXT,
  ADD COLUMN playground_response       TEXT,
  ADD COLUMN playground_response_meta  JSONB,
  ADD COLUMN share_slug                VARCHAR(30) UNIQUE,
  ADD COLUMN is_public                 BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX idx_generated_prompts_is_public_created_at ON generated_prompts (is_public, created_at);
