-- =====================================================================
-- PHASE 1 - File 6/9: Generated Prompts (Epic 3 generate + Epic 4)
-- ai_score, ai_refined, playground_*, share_slug, is_public
-- KHONG co o day - se ALTER them o Phase 2 (Epic 6)
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

  created_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at               TIMESTAMP
);

CREATE INDEX idx_generated_prompts_user_id_created_at ON generated_prompts (user_id, created_at);
CREATE INDEX idx_generated_prompts_template_id ON generated_prompts (template_id);
CREATE INDEX idx_generated_prompts_workspace_id ON generated_prompts (workspace_id);
CREATE INDEX idx_generated_prompts_deleted_at ON generated_prompts (deleted_at) WHERE deleted_at IS NULL;
