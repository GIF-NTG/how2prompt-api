-- =====================================================================
-- PHASE 3 - File 2/4: Follows + Reports (US-8.3, US-8.5)
-- reports.target_type whitelist gom ca 'user' (bao cao nguoi dung vi
-- pham, khong chi noi dung) - khong can doi schema, chi whitelist dung
-- o service.
-- =====================================================================

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
