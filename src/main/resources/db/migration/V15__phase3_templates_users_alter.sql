-- =====================================================================
-- PHASE 3 - File 4/4: ALTER templates + users
-- Trending (US-8.4): giu don gian, sort theo usage_count/view_count
-- tong (khong them bang template_daily_stats theo window) - da chot.
-- =====================================================================

ALTER TABLE templates
  ADD COLUMN upvote_count   INT NOT NULL DEFAULT 0,
  ADD COLUMN downvote_count INT NOT NULL DEFAULT 0,
  ADD COLUMN view_count     BIGINT NOT NULL DEFAULT 0;

ALTER TABLE users
  ADD COLUMN username VARCHAR(50) UNIQUE;
