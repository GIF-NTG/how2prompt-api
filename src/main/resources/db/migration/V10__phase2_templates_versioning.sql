-- =====================================================================
-- PHASE 2 - File 1/2: Template Customization & Versioning (Epic 7)
-- =====================================================================

ALTER TABLE templates
  ADD COLUMN forked_from_template_id UUID REFERENCES templates(id),
  ADD COLUMN forked_from_version_id  UUID,
  ADD COLUMN fork_count              INT NOT NULL DEFAULT 0,
  ADD COLUMN rejection_reason        TEXT,
  ADD COLUMN quality_score           NUMERIC(4,2);

ALTER TABLE templates
  ADD CONSTRAINT fk_templates_forked_from_version_id
    FOREIGN KEY (forked_from_version_id) REFERENCES template_versions(id);

CREATE INDEX idx_templates_forked_from_template_id ON templates (forked_from_template_id);

ALTER TABLE template_versions
  ADD COLUMN changelog TEXT;

-- Ghi chu: gia tri enum moi phat sinh, KHONG can doi schema (VARCHAR khong co CHECK constraint):
--   templates.status      them gia tri 'pending' (US-7.4: user submit -> cho admin duyet)
--   templates.author_type them gia tri 'user'    (Phase 1 mac dinh chi co 'admin')
