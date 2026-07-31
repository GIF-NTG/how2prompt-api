-- =====================================================================
-- R__03_demo_taxonomy_and_mappings.sql
-- Taxonomy Data and Mappings for Demo Purposes
-- Contains 5 Categories, 5 Tags, and mappings for the 10 seed templates.
-- =====================================================================

-- =============================================================
-- 1. CATEGORIES (5 Categories)
-- =============================================================
INSERT INTO categories (id, slug, name_i18n, description_i18n, is_active, created_at, updated_at) VALUES
('cat00000-0000-0000-0000-000000000001', 'software-development', '{"en": "Software Development", "vi": "Phát triển Phần mềm"}', '{}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat00000-0000-0000-0000-000000000002', 'human-resources', '{"en": "Human Resources", "vi": "Nhân sự"}', '{}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat00000-0000-0000-0000-000000000003', 'marketing-seo', '{"en": "Marketing & SEO", "vi": "Tiếp thị & SEO"}', '{}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat00000-0000-0000-0000-000000000004', 'creative-writing', '{"en": "Creative Writing", "vi": "Viết Sáng tạo"}', '{}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cat00000-0000-0000-0000-000000000005', 'business-strategy', '{"en": "Business Strategy", "vi": "Chiến lược Kinh doanh"}', '{}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- =============================================================
-- 2. TAGS (5 Tags)
-- =============================================================
INSERT INTO tags (id, slug, name, usage_count, created_at) VALUES
('tag00000-0000-0000-0000-000000000001', 'optimization', 'Optimization', 0, CURRENT_TIMESTAMP),
('tag00000-0000-0000-0000-000000000002', 'automation', 'Automation', 0, CURRENT_TIMESTAMP),
('tag00000-0000-0000-0000-000000000003', 'analysis', 'Analysis', 0, CURRENT_TIMESTAMP),
('tag00000-0000-0000-0000-000000000004', 'content-creation', 'Content Creation', 0, CURRENT_TIMESTAMP),
('tag00000-0000-0000-0000-000000000005', 'planning', 'Planning', 0, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- =============================================================
-- 3. TEMPLATE CATEGORIES MAPPING
-- =============================================================
INSERT INTO template_categories (template_id, category_id) VALUES
-- T1: Black-box Test Case -> Software Development
('c0000000-0000-0000-0000-000000000002', 'cat00000-0000-0000-0000-000000000001'),
-- T2: Unity C# Script -> Software Development
('c0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000001'),
-- T3: Manga Character -> Creative Writing
('c0000000-0000-0000-0000-000000000004', 'cat00000-0000-0000-0000-000000000004'),
-- T4: HR Job Description -> Human Resources
('c0000000-0000-0000-0000-000000000005', 'cat00000-0000-0000-0000-000000000002'),
-- T5: B2B Cold Outreach -> Marketing & SEO
('c0000000-0000-0000-0000-000000000006', 'cat00000-0000-0000-0000-000000000003'),
-- T6: SEO Blog Post -> Marketing & SEO
('c0000000-0000-0000-0000-000000000007', 'cat00000-0000-0000-0000-000000000003'),
-- T7: React Component -> Software Development
('c0000000-0000-0000-0000-000000000008', 'cat00000-0000-0000-0000-000000000001'),
-- T8: Startup Pitch Deck -> Business Strategy
('c0000000-0000-0000-0000-000000000009', 'cat00000-0000-0000-0000-000000000005'),
-- T9: SQL Query Tuner -> Software Development
('c0000000-0000-0000-0000-000000000010', 'cat00000-0000-0000-0000-000000000001'),
-- T10: Product Release Notes -> Business Strategy
('c0000000-0000-0000-0000-000000000011', 'cat00000-0000-0000-0000-000000000005'),
-- T11: Universal Prompt Framework -> Business Strategy
('c0000000-0000-0000-0000-000000000012', 'cat00000-0000-0000-0000-000000000005'),
-- T12: Quick Task Delegator -> Business Strategy
('c0000000-0000-0000-0000-000000000013', 'cat00000-0000-0000-0000-000000000005')
ON CONFLICT (template_id, category_id) DO NOTHING;

-- =============================================================
-- 4. TEMPLATE TAGS MAPPING
-- =============================================================
INSERT INTO template_tags (template_id, tag_id) VALUES
-- T1: Analysis, Automation
('c0000000-0000-0000-0000-000000000002', 'tag00000-0000-0000-0000-000000000003'),
('c0000000-0000-0000-0000-000000000002', 'tag00000-0000-0000-0000-000000000002'),
-- T2: Optimization
('c0000000-0000-0000-0000-000000000003', 'tag00000-0000-0000-0000-000000000001'),
-- T3: Analysis, Content Creation
('c0000000-0000-0000-0000-000000000004', 'tag00000-0000-0000-0000-000000000003'),
('c0000000-0000-0000-0000-000000000004', 'tag00000-0000-0000-0000-000000000004'),
-- T4: Content Creation
('c0000000-0000-0000-0000-000000000005', 'tag00000-0000-0000-0000-000000000004'),
-- T5: Automation, Content Creation
('c0000000-0000-0000-0000-000000000006', 'tag00000-0000-0000-0000-000000000002'),
('c0000000-0000-0000-0000-000000000006', 'tag00000-0000-0000-0000-000000000004'),
-- T6: Planning, Content Creation
('c0000000-0000-0000-0000-000000000007', 'tag00000-0000-0000-0000-000000000005'),
('c0000000-0000-0000-0000-000000000007', 'tag00000-0000-0000-0000-000000000004'),
-- T7: Optimization, Automation
('c0000000-0000-0000-0000-000000000008', 'tag00000-0000-0000-0000-000000000001'),
('c0000000-0000-0000-0000-000000000008', 'tag00000-0000-0000-0000-000000000002'),
-- T8: Planning, Content Creation
('c0000000-0000-0000-0000-000000000009', 'tag00000-0000-0000-0000-000000000005'),
('c0000000-0000-0000-0000-000000000009', 'tag00000-0000-0000-0000-000000000004'),
-- T9: Optimization, Analysis
('c0000000-0000-0000-0000-000000000010', 'tag00000-0000-0000-0000-000000000001'),
('c0000000-0000-0000-0000-000000000010', 'tag00000-0000-0000-0000-000000000003'),
-- T10: Content Creation, Planning
('c0000000-0000-0000-0000-000000000011', 'tag00000-0000-0000-0000-000000000004'),
('c0000000-0000-0000-0000-000000000011', 'tag00000-0000-0000-0000-000000000005'),
-- T11: Planning, Content Creation
('c0000000-0000-0000-0000-000000000012', 'tag00000-0000-0000-0000-000000000005'),
('c0000000-0000-0000-0000-000000000012', 'tag00000-0000-0000-0000-000000000004'),
-- T12: Automation, Content Creation
('c0000000-0000-0000-0000-000000000013', 'tag00000-0000-0000-0000-000000000002'),
('c0000000-0000-0000-0000-000000000013', 'tag00000-0000-0000-0000-000000000004')
ON CONFLICT (template_id, tag_id) DO NOTHING;
