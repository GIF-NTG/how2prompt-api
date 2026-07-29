-- =====================================================================
-- PHASE 3 - File 1/4: Votes + Comments (US-8.1, US-8.2)
-- Polymorphic pattern (target_type + target_id) - Postgres KHONG tu
-- kiem tra target_id co ton tai dung bang khong. Tang Service phai
-- tu validate whitelist target_type va target_id thuc su ton tai.
-- =====================================================================

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

CREATE TRIGGER set_updated_at_comments BEFORE UPDATE ON comments
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
