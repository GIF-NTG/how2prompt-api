-- =====================================================================
-- PHASE 1 - File 9/9: Triggers
-- Trigger cho comments (Phase 3) va subscriptions (Phase 4) se duoc
-- them cung file tao bang tuong ung, khong dat o day.
-- =====================================================================

-- 9.1 Auto-update updated_at on row change (reused across tables)
CREATE OR REPLACE FUNCTION trg_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at_users BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_workspaces BEFORE UPDATE ON workspaces
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_templates BEFORE UPDATE ON templates
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_template_variants BEFORE UPDATE ON template_variants
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_generated_prompts BEFORE UPDATE ON generated_prompts
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_ai_models BEFORE UPDATE ON ai_models
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();
CREATE TRIGGER set_updated_at_categories BEFORE UPDATE ON categories
  FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();

-- 9.2 Auto-update templates.search_vector from title_i18n + description_i18n
CREATE OR REPLACE FUNCTION trg_templates_search_vector()
RETURNS TRIGGER AS $$
BEGIN
  NEW.search_vector :=
    setweight(to_tsvector('simple', coalesce(NEW.title_i18n->>'en', '')), 'A') ||
    setweight(to_tsvector('simple', coalesce(NEW.title_i18n->>'vi', '')), 'A') ||
    setweight(to_tsvector('simple', coalesce(NEW.description_i18n->>'en', '')), 'B') ||
    setweight(to_tsvector('simple', coalesce(NEW.description_i18n->>'vi', '')), 'B');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_templates_search_vector
  BEFORE INSERT OR UPDATE OF title_i18n, description_i18n ON templates
  FOR EACH ROW EXECUTE FUNCTION trg_templates_search_vector();
