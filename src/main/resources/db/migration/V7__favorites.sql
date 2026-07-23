-- =====================================================================
-- PHASE 1 - File 7/9: Favorites (US-4.3)
-- File sql goc gop chung "Community" (votes/comments/follows/reports)
-- vao 1 comment block va ghi "schema ready Phase 1" - KHONG dung.
-- Chi favorites la Phase 1 that su. votes/comments/follows/reports
-- se tao o Phase 3 (Epic 8).
-- =====================================================================

CREATE TABLE favorites (
  user_id     UUID NOT NULL REFERENCES users(id),
  template_id UUID NOT NULL REFERENCES templates(id),
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, template_id)
);

CREATE INDEX idx_favorites_template_id ON favorites (template_id);
