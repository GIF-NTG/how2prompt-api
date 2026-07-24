-- =====================================================================
-- PHASE 1 - File 1/9: Extensions
-- =====================================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- fuzzy search (tags)
