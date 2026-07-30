-- =====================================================================
-- V22: Seed data for local development and testing
--
-- PURPOSE : Bootstrap a minimal dataset so developers can log in,
--           browse templates, generate prompts, and test favorites
--           immediately after running migrations.
--
-- LOGIN CREDENTIALS (both users):
--   Password (plain-text) : password
--   BCrypt hash            : $2a$10$wB5E8U21oJb3F8y01.aMzeO9mZ6.y4l.G/t3I5y2oW.g.a7pU8R.q
--
-- UUID ALLOCATION:
--   Admin user           : 11111111-1111-1111-1111-111111111111
--   Normal user          : 22222222-2222-2222-2222-222222222222
--   Admin identity       : 11111111-1111-1111-1111-aaaaaaaaaaaa
--   Normal user identity : 22222222-2222-2222-2222-aaaaaaaaaaaa
--   Admin workspace      : aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa
--   User workspace       : bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb
--   Template             : cccccccc-cccc-cccc-cccc-cccccccccccc
--   Template version     : dddddddd-dddd-dddd-dddd-dddddddddddd
--   Variable (role)      : eeeeeeee-0001-0001-0001-eeeeeeeeeeee
--   Variable (context)   : eeeeeeee-0002-0002-0002-eeeeeeeeeeee
--   Variable (task)      : eeeeeeee-0003-0003-0003-eeeeeeeeeeee
--   Variable (constraint): eeeeeeee-0004-0004-0004-eeeeeeeeeeee
--   Generated prompt 1   : ffffffff-0001-0001-0001-ffffffffffff
--   Generated prompt 2   : ffffffff-0002-0002-0002-ffffffffffff
-- =====================================================================


-- =============================================================
-- 1. USERS
-- =============================================================
INSERT INTO users (id, email, password_hash, full_name, username, is_admin, email_verified_at, locale, timezone, plan)
VALUES
  (
    '11111111-1111-1111-1111-111111111111',
    'admin@how2prompt.com',
    '$2a$12$XTSt6f/ARjR47z49Q9PKBeQJMG.D4GUr2lo3MixEdq9tHbh2ZNG3S',
    'Admin User',
    'admin',
    true,
    CURRENT_TIMESTAMP,
    'en',
    'Asia/Ho_Chi_Minh',
    'free'
  ),
  (
    '22222222-2222-2222-2222-222222222222',
    'user@how2prompt.com',
    '$2a$12$XTSt6f/ARjR47z49Q9PKBeQJMG.D4GUr2lo3MixEdq9tHbh2ZNG3S',
    'Normal User',
    'normaluser',
    false,
    CURRENT_TIMESTAMP,
    'en',
    'Asia/Ho_Chi_Minh',
    'free'
  )
ON CONFLICT (id) DO NOTHING;


-- =============================================================
-- 2. USER IDENTITIES  (provider = 'LOCAL')
-- =============================================================
INSERT INTO user_identities (id, user_id, provider, provider_uid, email)
VALUES
  (
    '11111111-1111-1111-1111-aaaaaaaaaaaa',
    '11111111-1111-1111-1111-111111111111',
    'LOCAL',
    'admin@how2prompt.com',
    'admin@how2prompt.com'
  ),
  (
    '22222222-2222-2222-2222-aaaaaaaaaaaa',
    '22222222-2222-2222-2222-222222222222',
    'LOCAL',
    'user@how2prompt.com',
    'user@how2prompt.com'
  )
ON CONFLICT (id) DO NOTHING;


-- =============================================================
-- 3. WORKSPACES  (personal workspaces — required for FK integrity)
-- =============================================================
INSERT INTO workspaces (id, slug, name, type, owner_id, settings, plan)
VALUES
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'admin-personal',
    'Admin''s Personal Workspace',
    'personal',
    '11111111-1111-1111-1111-111111111111',
    '{}',
    'free'
  ),
  (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'user-personal',
    'Normal User''s Personal Workspace',
    'personal',
    '22222222-2222-2222-2222-222222222222',
    '{}',
    'free'
  )
ON CONFLICT (id) DO NOTHING;


-- =============================================================
-- 4. TEMPLATE  (owned by Admin, in Admin's workspace)
--    current_version_id is NULL initially (circular FK — updated below)
-- =============================================================
INSERT INTO templates (
  id, workspace_id, slug, title_i18n, description_i18n,
  author_id, author_type, is_official, is_public, status,
  current_version_id, published_at
)
VALUES (
  'cccccccc-cccc-cccc-cccc-cccccccccccc',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'prompt-engineering-assistant',
  '{"en": "Prompt Engineering Assistant", "vi": "Trợ lý Kỹ thuật Prompt"}',
  '{"en": "A comprehensive template that guides you through crafting high-quality, structured AI prompts. Provide your desired role, context, task, and constraints — the template synthesizes them into an optimized, ready-to-use prompt following industry best practices.", "vi": "Một mẫu toàn diện hướng dẫn bạn tạo các prompt AI chất lượng cao, có cấu trúc. Cung cấp vai trò, bối cảnh, nhiệm vụ và ràng buộc mong muốn — mẫu sẽ tổng hợp chúng thành một prompt tối ưu, sẵn sàng sử dụng theo các phương pháp tốt nhất trong ngành."}',
  '11111111-1111-1111-1111-111111111111',
  'admin',
  true,
  true,
  'published',
  NULL,
  CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;


-- =============================================================
-- 5. TEMPLATE VERSION  (v1, is_current = true)
-- =============================================================
INSERT INTO template_versions (
  id, template_id, version_number, prompt_body, system_prompt,
  example_output, guide_i18n, is_current, created_by, changelog
)
VALUES (
  'dddddddd-dddd-dddd-dddd-dddddddddddd',
  'cccccccc-cccc-cccc-cccc-cccccccccccc',
  1,
  -- prompt_body: uses {{role}}, {{context}}, {{task}}, {{constraint}} placeholders
  E'You are {{role}}.\n\n## Context\n{{context}}\n\n## Task\n{{task}}\n\n## Constraints & Guidelines\n{{constraint}}\n\n---\n\nBased on everything above, deliver a response that:\n1. Fully addresses the task with depth and precision\n2. Stays grounded in the provided context\n3. Strictly adheres to every constraint listed\n4. Uses clear structure with headings, bullet points, or numbered lists where appropriate\n5. Is immediately actionable — no filler, no fluff',
  -- system_prompt
  E'You are an expert Prompt Engineer specializing in crafting high-quality, structured prompts for large language models. Your goal is to synthesize user-provided components (role, context, task, and constraints) into a single, optimized response that follows industry best practices for clarity, specificity, and effectiveness. Always produce output that is ready to use without further editing.',
  -- example_output
  E'You are a senior Python developer and technical writer with 10 years of experience.\n\n## Context\nI am creating a beginner-friendly tutorial series for a coding bootcamp. The audience has basic programming knowledge but is new to Python.\n\n## Task\nWrite a comprehensive tutorial on Python list comprehensions, covering basic syntax, filtering, nested comprehensions, and common use cases with practical examples.\n\n## Constraints & Guidelines\nKeep the tutorial under 2000 words. Use simple language suitable for beginners. Include at least 5 runnable code examples. Avoid advanced topics like generator expressions.\n\n---\n\nBased on everything above, deliver a response that:\n1. Fully addresses the task with depth and precision\n2. Stays grounded in the provided context\n3. Strictly adheres to every constraint listed\n4. Uses clear structure with headings, bullet points, or numbered lists where appropriate\n5. Is immediately actionable — no filler, no fluff',
  -- guide_i18n
  '{"en": "Fill in each field to define the building blocks of your prompt. The Role sets the AI''s persona and expertise. Context provides the background situation. Task defines exactly what you want the AI to do. Constraints set the boundaries and formatting rules. The template will merge them into one polished, ready-to-use prompt.", "vi": "Điền vào từng trường để xác định các thành phần cơ bản của prompt. Vai trò thiết lập nhân cách và chuyên môn của AI. Bối cảnh cung cấp tình huống nền. Nhiệm vụ xác định chính xác những gì bạn muốn AI thực hiện. Ràng buộc đặt ra các giới hạn và quy tắc định dạng. Mẫu sẽ hợp nhất tất cả thành một prompt hoàn chỉnh, sẵn sàng sử dụng."}',
  true,
  '11111111-1111-1111-1111-111111111111',
  'Initial version — 4 core variables: role, context, task, constraint'
)
ON CONFLICT (id) DO NOTHING;


-- =============================================================
-- 6. TEMPLATE VARIABLES  (role, context, task, constraint)
-- =============================================================
INSERT INTO template_variables (
  id, template_version_id, var_key, label_i18n, description_i18n,
  placeholder_i18n, help_text_i18n, input_type, is_required,
  default_value, options, validation, sort_order
)
VALUES
  -- 6.1  role
  (
    'eeeeeeee-0001-0001-0001-eeeeeeeeeeee',
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    'role',
    '{"en": "Role", "vi": "Vai trò"}',
    '{"en": "Define the AI''s persona, expertise, and professional background. A specific role produces more focused and authoritative responses.", "vi": "Xác định nhân cách, chuyên môn và nền tảng nghề nghiệp của AI. Vai trò cụ thể tạo ra phản hồi tập trung và có thẩm quyền hơn."}',
    '{"en": "e.g., a senior data scientist with expertise in NLP and 8 years of industry experience", "vi": "VD: một nhà khoa học dữ liệu cao cấp chuyên về NLP với 8 năm kinh nghiệm"}',
    '{"en": "Be as specific as possible — include years of experience, domain expertise, and professional title for best results.", "vi": "Hãy cụ thể nhất có thể — bao gồm số năm kinh nghiệm, chuyên môn lĩnh vực và chức danh nghề nghiệp để có kết quả tốt nhất."}',
    'text',
    true,
    NULL,
    '[]',
    '{"minLength": 10, "maxLength": 500}',
    0
  ),
  -- 6.2  context
  (
    'eeeeeeee-0002-0002-0002-eeeeeeeeeeee',
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    'context',
    '{"en": "Context", "vi": "Bối cảnh"}',
    '{"en": "Describe the background situation, target audience, project scope, or environment in which the AI''s output will be used.", "vi": "Mô tả tình huống nền, đối tượng mục tiêu, phạm vi dự án hoặc môi trường mà đầu ra của AI sẽ được sử dụng."}',
    '{"en": "e.g., I am building an onboarding flow for a B2B SaaS product targeting enterprise HR managers", "vi": "VD: Tôi đang xây dựng quy trình onboarding cho sản phẩm B2B SaaS nhắm đến quản lý nhân sự doanh nghiệp"}',
    '{"en": "Include who the audience is, what project or situation this is for, and any relevant background the AI needs to know.", "vi": "Bao gồm đối tượng là ai, dự án hoặc tình huống này dành cho gì, và bất kỳ thông tin nền nào AI cần biết."}',
    'textarea',
    true,
    NULL,
    '[]',
    '{"minLength": 20, "maxLength": 2000}',
    1
  ),
  -- 6.3  task
  (
    'eeeeeeee-0003-0003-0003-eeeeeeeeeeee',
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    'task',
    '{"en": "Task", "vi": "Nhiệm vụ"}',
    '{"en": "Clearly state what you want the AI to produce. Be specific about deliverables, format, depth, and scope.", "vi": "Nêu rõ những gì bạn muốn AI tạo ra. Cụ thể về sản phẩm đầu ra, định dạng, độ sâu và phạm vi."}',
    '{"en": "e.g., Write a 5-step onboarding email sequence that educates new users about core features and drives activation", "vi": "VD: Viết chuỗi 5 email onboarding hướng dẫn người dùng mới về các tính năng cốt lõi và thúc đẩy kích hoạt"}',
    '{"en": "The more specific and measurable your task description, the better the AI''s output will be. Include deliverable format if relevant.", "vi": "Mô tả nhiệm vụ càng cụ thể và đo lường được, đầu ra của AI càng tốt. Bao gồm định dạng sản phẩm nếu phù hợp."}',
    'textarea',
    true,
    NULL,
    '[]',
    '{"minLength": 20, "maxLength": 2000}',
    2
  ),
  -- 6.4  constraint
  (
    'eeeeeeee-0004-0004-0004-eeeeeeeeeeee',
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    'constraint',
    '{"en": "Constraints", "vi": "Ràng buộc"}',
    '{"en": "List any rules, limitations, formatting requirements, tone guidelines, or things the AI should avoid.", "vi": "Liệt kê các quy tắc, giới hạn, yêu cầu định dạng, hướng dẫn giọng điệu hoặc những điều AI nên tránh."}',
    '{"en": "e.g., Use a professional but friendly tone. Each email must be under 200 words. Do not mention competitor products.", "vi": "VD: Sử dụng giọng điệu chuyên nghiệp nhưng thân thiện. Mỗi email phải dưới 200 từ. Không đề cập sản phẩm đối thủ."}',
    '{"en": "Constraints act as guardrails. Include tone, length limits, things to avoid, required formatting, and any non-negotiable rules.", "vi": "Ràng buộc đóng vai trò như lan can bảo vệ. Bao gồm giọng điệu, giới hạn độ dài, những điều cần tránh, định dạng bắt buộc và các quy tắc không thể thương lượng."}',
    'textarea',
    false,
    'No specific constraints. Use your best professional judgment.',
    '[]',
    '{"maxLength": 2000}',
    3
  )
ON CONFLICT (id) DO NOTHING;


-- =============================================================
-- 7. RESOLVE CIRCULAR FK: set templates.current_version_id
-- =============================================================
UPDATE templates
SET    current_version_id = 'dddddddd-dddd-dddd-dddd-dddddddddddd'
WHERE  id                 = 'cccccccc-cccc-cccc-cccc-cccccccccccc';


-- =============================================================
-- 8. GENERATED PROMPTS  (Normal User, in User's workspace)
--    ai_model_id is intentionally NULL (no AI model seed data)
-- =============================================================
INSERT INTO generated_prompts (
  id, user_id, workspace_id, template_id, template_version_id,
  ai_model_id, title, input_values, extra_instructions, final_prompt
)
VALUES
  -- 8.1  Generated Prompt 1: Python Tutorial
  (
    'ffffffff-0001-0001-0001-ffffffffffff',
    '22222222-2222-2222-2222-222222222222',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    NULL,
    'Python List Comprehensions Tutorial',
    '{
      "role": "a senior Python developer and technical writer with 10 years of experience in building developer education content",
      "context": "I am creating a beginner-friendly tutorial series for a coding bootcamp. The audience has basic programming knowledge in any language but is completely new to Python. The tutorials will be published on our learning platform and should stand alone as self-contained lessons.",
      "task": "Write a comprehensive tutorial on Python list comprehensions, covering basic syntax, filtering with conditionals, nested comprehensions, and common real-world use cases. Include practical, runnable code examples with expected output for each concept.",
      "constraint": "Keep the tutorial under 2000 words. Use simple language suitable for beginners — avoid jargon without explanation. Include at least 5 runnable code examples with their expected output. Avoid advanced topics like generator expressions or walrus operator. Use Markdown formatting with clear section headings."
    }',
    NULL,
    E'You are a senior Python developer and technical writer with 10 years of experience in building developer education content.\n\n## Context\nI am creating a beginner-friendly tutorial series for a coding bootcamp. The audience has basic programming knowledge in any language but is completely new to Python. The tutorials will be published on our learning platform and should stand alone as self-contained lessons.\n\n## Task\nWrite a comprehensive tutorial on Python list comprehensions, covering basic syntax, filtering with conditionals, nested comprehensions, and common real-world use cases. Include practical, runnable code examples with expected output for each concept.\n\n## Constraints & Guidelines\nKeep the tutorial under 2000 words. Use simple language suitable for beginners — avoid jargon without explanation. Include at least 5 runnable code examples with their expected output. Avoid advanced topics like generator expressions or walrus operator. Use Markdown formatting with clear section headings.\n\n---\n\nBased on everything above, deliver a response that:\n1. Fully addresses the task with depth and precision\n2. Stays grounded in the provided context\n3. Strictly adheres to every constraint listed\n4. Uses clear structure with headings, bullet points, or numbered lists where appropriate\n5. Is immediately actionable — no filler, no fluff'
  ),
  -- 8.2  Generated Prompt 2: Marketing Email
  (
    'ffffffff-0002-0002-0002-ffffffffffff',
    '22222222-2222-2222-2222-222222222222',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    NULL,
    'AI Analytics Dashboard Launch Email',
    '{
      "role": "a seasoned digital marketing strategist and email copywriter who has driven product launches for multiple successful SaaS startups",
      "context": "Our SaaS startup is launching a new AI-powered analytics dashboard called InsightFlow. The target audience is small-to-medium business owners who currently rely on spreadsheets and manual reporting for data analysis. We are offering a free 14-day trial with no credit card required. The launch date is next Monday.",
      "task": "Write a compelling product launch email that highlights the three key benefits (automated reports, natural-language queries, and real-time dashboards), includes a clear call-to-action for the free 14-day trial, and creates a sense of urgency tied to the launch week.",
      "constraint": "The email body should be under 300 words. Use a conversational but professional tone. Include a subject line and a preview text (under 90 characters). Do not use technical jargon, buzzwords, or exclamation marks. Structure the email with a hook, benefits section, social proof placeholder, and a single prominent CTA button."
    }',
    'Add a P.S. line at the bottom with a secondary link to a 2-minute product demo video.',
    E'You are a seasoned digital marketing strategist and email copywriter who has driven product launches for multiple successful SaaS startups.\n\n## Context\nOur SaaS startup is launching a new AI-powered analytics dashboard called InsightFlow. The target audience is small-to-medium business owners who currently rely on spreadsheets and manual reporting for data analysis. We are offering a free 14-day trial with no credit card required. The launch date is next Monday.\n\n## Task\nWrite a compelling product launch email that highlights the three key benefits (automated reports, natural-language queries, and real-time dashboards), includes a clear call-to-action for the free 14-day trial, and creates a sense of urgency tied to the launch week.\n\n## Constraints & Guidelines\nThe email body should be under 300 words. Use a conversational but professional tone. Include a subject line and a preview text (under 90 characters). Do not use technical jargon, buzzwords, or exclamation marks. Structure the email with a hook, benefits section, social proof placeholder, and a single prominent CTA button.\n\n---\n\nBased on everything above, deliver a response that:\n1. Fully addresses the task with depth and precision\n2. Stays grounded in the provided context\n3. Strictly adheres to every constraint listed\n4. Uses clear structure with headings, bullet points, or numbered lists where appropriate\n5. Is immediately actionable — no filler, no fluff'
  )
ON CONFLICT (id) DO NOTHING;


-- =============================================================
-- 9. FAVORITES  (Normal User → Template)
-- =============================================================
INSERT INTO favorites (user_id, template_id)
VALUES (
  '22222222-2222-2222-2222-222222222222',
  'cccccccc-cccc-cccc-cccc-cccccccccccc'
)
ON CONFLICT (user_id, template_id) DO NOTHING;
