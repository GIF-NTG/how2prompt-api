-- =====================================================================
-- R__02_expanded_demo_data.sql
-- Expanded Seed Data for Demo Purposes
-- Contains 10 templates and 5 generated prompts.
-- =====================================================================

-- =============================================================
-- 1. TEMPLATES (10 Templates)
-- =============================================================
INSERT INTO templates (
  id, workspace_id, slug, title_i18n, description_i18n,
  author_id, author_type, is_official, is_public, status,
  current_version_id, published_at
) VALUES
-- T1: Black-box Test Case Generator
(
  'c0000000-0000-0000-0000-000000000002',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'blackbox-test-case-generator',
  '{"en": "Black-box Test Case Generator", "vi": "Tạo Ca kiểm thử Hộp đen (BVA/ECP)"}',
  '{"en": "Automatically generate comprehensive black-box test cases using Boundary Value Analysis and Equivalence Class Partitioning.", "vi": "Tự động tạo các ca kiểm thử hộp đen toàn diện bằng Phân tích Giá trị Biên và Phân vùng Lớp Tương đương."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T2: Unity C# Script Optimizer
(
  'c0000000-0000-0000-0000-000000000003',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'unity-csharp-optimizer',
  '{"en": "Unity C# Script Optimizer", "vi": "Tối ưu hóa mã C# Unity"}',
  '{"en": "Identify and fix performance bottlenecks in your Unity C# scripts to achieve a smooth 60+ FPS.", "vi": "Xác định và sửa các nút thắt hiệu suất trong mã C# Unity của bạn để đạt được 60+ FPS mượt mà."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T3: Manga/Anime Character Motivation Analyzer
(
  'c0000000-0000-0000-0000-000000000004',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'manga-character-motivation',
  '{"en": "Character Motivation Analyzer", "vi": "Phân tích Động lực Nhân vật"}',
  '{"en": "Deep dive into a character''s psyche to establish believable motivations for their actions in your story.", "vi": "Đi sâu vào tâm lý nhân vật để thiết lập những động lực đáng tin cậy cho hành động của họ trong câu chuyện của bạn."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T4: HR Job Description Writer
(
  'c0000000-0000-0000-0000-000000000005',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'hr-job-description',
  '{"en": "HR Job Description Writer", "vi": "Viết Mô tả Công việc HR"}',
  '{"en": "Generate compelling and inclusive job descriptions that attract top talent to your organization.", "vi": "Tạo các mô tả công việc hấp dẫn và bao trùm nhằm thu hút nhân tài hàng đầu đến với tổ chức của bạn."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T5: B2B Cold Outreach Email
(
  'c0000000-0000-0000-0000-000000000006',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'b2b-cold-outreach-email',
  '{"en": "B2B Cold Outreach Email", "vi": "Email Tiếp cận Khách hàng B2B"}',
  '{"en": "Craft high-converting cold emails tailored to your target industry''s specific pain points.", "vi": "Soạn thảo email lạnh có tỷ lệ chuyển đổi cao được điều chỉnh cho các điểm đau cụ thể của ngành mục tiêu."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T6: SEO Blog Post Outliner
(
  'c0000000-0000-0000-0000-000000000007',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'seo-blog-post-outliner',
  '{"en": "SEO Blog Post Outliner", "vi": "Lập Dàn ý Bài viết Blog SEO"}',
  '{"en": "Structure your blog posts for maximum search engine visibility and reader engagement.", "vi": "Cấu trúc các bài viết blog của bạn để đạt khả năng hiển thị tối đa trên công cụ tìm kiếm và sự tương tác của người đọc."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T7: React Component Refactoring Assistant
(
  'c0000000-0000-0000-0000-000000000008',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'react-component-refactoring',
  '{"en": "React Component Refactoring Assistant", "vi": "Trợ lý Tái cấu trúc Component React"}',
  '{"en": "Modernize your React components, migrate to hooks, and fix performance issues.", "vi": "Hiện đại hóa các component React của bạn, di chuyển sang hooks và khắc phục các vấn đề hiệu suất."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T8: Startup Pitch Deck Storyboard
(
  'c0000000-0000-0000-0000-000000000009',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'startup-pitch-deck-storyboard',
  '{"en": "Startup Pitch Deck Storyboard", "vi": "Kịch bản Pitch Deck Khởi nghiệp"}',
  '{"en": "Create a persuasive narrative flow for your startup pitch deck to captivate investors.", "vi": "Tạo một luồng kể chuyện đầy thuyết phục cho bản pitch deck khởi nghiệp của bạn để thu hút các nhà đầu tư."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T9: SQL Query Performance Tuner
(
  'c0000000-0000-0000-0000-000000000010',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'sql-query-performance-tuner',
  '{"en": "SQL Query Performance Tuner", "vi": "Tối ưu hóa Hiệu suất Truy vấn SQL"}',
  '{"en": "Analyze and optimize slow SQL queries by suggesting indexes, joins refactoring, and execution plan improvements.", "vi": "Phân tích và tối ưu hóa các truy vấn SQL chậm bằng cách đề xuất các chỉ mục, tái cấu trúc kết nối và cải thiện kế hoạch thực thi."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T10: Product Release Notes Generator
(
  'c0000000-0000-0000-0000-000000000011',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'product-release-notes',
  '{"en": "Product Release Notes Generator", "vi": "Tạo Ghi chú Phát hành Sản phẩm"}',
  '{"en": "Turn technical changelogs into user-friendly, engaging release notes.", "vi": "Biến các nhật ký thay đổi kỹ thuật thành các ghi chú phát hành thân thiện và hấp dẫn với người dùng."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T11: Universal Prompt Framework
(
  'c0000000-0000-0000-0000-000000000012',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'universal-prompt-framework',
  '{"en": "Universal Prompt Framework", "vi": "Khung Prompt Phổ quát"}',
  '{"en": "A highly flexible, standard framework that applies to almost any task.", "vi": "Một khung tiêu chuẩn rất linh hoạt áp dụng cho hầu hết mọi tác vụ."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
),
-- T12: Quick Task Delegator
(
  'c0000000-0000-0000-0000-000000000013',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'quick-task-delegator',
  '{"en": "Quick Task Delegator", "vi": "Giao Việc Nhanh"}',
  '{"en": "A minimalist template for fast, everyday requests.", "vi": "Một mẫu tối giản cho các yêu cầu nhanh chóng hàng ngày."}',
  '11111111-1111-1111-1111-111111111111', 'admin', true, true, 'published', NULL, CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- =============================================================
-- 2. TEMPLATE VERSIONS
-- =============================================================
INSERT INTO template_versions (
  id, template_id, version_number, prompt_body, system_prompt,
  example_output, guide_i18n, is_current, created_by, changelog
) VALUES
-- T1
(
  'd0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 1,
  E'Act as a Senior QA Automation Engineer. I need comprehensive black-box test cases for the following feature.\n\n**Feature Description:**\n{{feature_description}}\n\n**Input Parameters:**\n{{input_parameters}}\n\n**Expected Outcomes:**\n{{expected_outcomes}}\n\nPlease generate a test suite applying Boundary Value Analysis (BVA) and Equivalence Class Partitioning (ECP). Format the output as a Markdown table with columns: Test Case ID, Description, Technique (BVA/ECP), Input Data, Expected Result.',
  E'You are a strict, detail-oriented QA expert focusing on edge cases, negative tests, and security boundaries.',
  E'Output will be a Markdown table listing test cases for an input field.',
  '{"en": "Describe your feature and the inputs to test.", "vi": "Mô tả tính năng và các đầu vào cần kiểm thử."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T2
(
  'd0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000003', 1,
  E'Act as a Unity Performance Optimization Expert. I have a script causing frame drops.\n\n**Unity Version:** {{unity_version}}\n\n**Performance Bottlenecks Noticed:**\n{{performance_bottlenecks}}\n\n**Script Code:**\n```csharp\n{{script_code}}\n```\n\nPlease refactor the code to eliminate GC allocations, avoid expensive calls in Update(), and use object pooling if necessary.',
  E'You are an expert Unity developer with deep knowledge of C# memory management and Unity-specific performance pitfalls.',
  E'Output will be the refactored C# script with explanatory comments.',
  '{"en": "Paste your C# script and mention the Unity version.", "vi": "Dán mã C# của bạn và đề cập đến phiên bản Unity."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T3
(
  'd0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000004', 1,
  E'Act as a Master Storyteller and Character Psychologist. I need to flesh out a character.\n\n**Character Name:** {{character_name}}\n\n**Background Story:**\n{{background_story}}\n\n**Current Conflict:**\n{{current_conflict}}\n\nProvide a detailed analysis of their intrinsic and extrinsic motivations, potential fatal flaws, and how their background dictates their response to the current conflict.',
  E'You are a narrative designer for top-tier anime and manga series.',
  E'Output will be a psychological profile of the character.',
  '{"en": "Describe your character''s past and present situation.", "vi": "Mô tả quá khứ và tình huống hiện tại của nhân vật của bạn."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T4
(
  'd0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000005', 1,
  E'Act as an Executive HR Recruiter. Write a job description for the following role.\n\n**Job Title:** {{job_title}}\n**Department:** {{department}}\n\n**Key Responsibilities:**\n{{key_responsibilities}}\n\n**Required Qualifications:**\n{{qualifications}}\n\nEnsure the tone is welcoming, uses inclusive language, and highlights career growth opportunities.',
  E'You are a progressive HR leader skilled in writing modern, attractive job postings.',
  E'Output will be a formatted job description ready for LinkedIn or a careers page.',
  '{"en": "Provide the basic job details to get a polished JD.", "vi": "Cung cấp các chi tiết công việc cơ bản để có một JD hoàn chỉnh."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T5
(
  'd0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000006', 1,
  E'Act as a B2B SaaS Sales Executive. Write a cold outreach email.\n\n**Target Industry:** {{target_industry}}\n**Pain Points:** {{pain_points}}\n**Our Value Proposition:** {{value_proposition}}\n\nThe email must have a catchy subject line, a personalized hook, directly address the pain point, present our value proposition softly, and end with a low-friction call to action (e.g., a quick 10-minute chat). Keep it under 150 words.',
  E'You are a master copywriter specializing in high-conversion B2B cold emails.',
  E'Output will be an email template with a subject line.',
  '{"en": "Who are you emailing and what problem do you solve?", "vi": "Bạn đang gửi email cho ai và bạn giải quyết vấn đề gì?"}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T6
(
  'd0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000007', 1,
  E'Act as an SEO Content Strategist. I need a blog post outline.\n\n**Target Audience:** {{target_audience}}\n**Primary Keyword:** {{primary_keyword}}\n**Secondary Keywords:** {{secondary_keywords}}\n\nProvide an outline including a working title, H2 and H3 headings, where to naturally insert the keywords, and a bulleted list of key points to cover in each section. Suggest an engaging hook for the introduction.',
  E'You are an expert in SEO and content marketing, focusing on search intent and readability.',
  E'Output will be a structured outline with heading tags.',
  '{"en": "Input your SEO keywords and target audience.", "vi": "Nhập các từ khóa SEO và đối tượng mục tiêu của bạn."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T7
(
  'd0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000008', 1,
  E'Act as a Senior React Developer. Refactor the following component.\n\n**Target React Version:** {{target_react_version}}\n**Specific Issues to Address:** {{specific_issues}}\n\n**Component Code:**\n```jsx\n{{component_code}}\n```\n\nEnsure best practices, replace class components with functional components if applicable, optimize re-renders using useMemo/useCallback if needed, and add concise comments explaining the changes.',
  E'You are a React performance expert with deep knowledge of the React lifecycle and hooks.',
  E'Output will be the refactored code and an explanation.',
  '{"en": "Paste your React code and describe what needs fixing.", "vi": "Dán mã React của bạn và mô tả những gì cần sửa."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T8
(
  'd0000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000009', 1,
  E'Act as a Venture Capitalist and Pitch Coach. I need a 10-slide pitch deck storyboard.\n\n**Product Name:** {{product_name}}\n**Target Market:** {{target_market}}\n**Problem Solved:** {{problem_solved}}\n**Business Model:** {{business_model}}\n\nFor each slide, provide a Title, Key Message (1 sentence), and Visual Suggestion (what the slide should show). Focus on narrative flow and investor priorities (market size, traction, unit economics).',
  E'You are an experienced startup founder who has raised millions from top-tier VC firms.',
  E'Output will be a slide-by-slide outline for a pitch deck.',
  '{"en": "Give the core details of your startup business.", "vi": "Cung cấp các chi tiết cốt lõi về doanh nghiệp khởi nghiệp của bạn."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T9
(
  'd0000000-0000-0000-0000-000000000010', 'c0000000-0000-0000-0000-000000000010', 1,
  E'Act as a Database Administrator (DBA). I have a slow performing SQL query.\n\n**Database Engine:** {{database_engine}}\n**Performance Issue Noticed:** {{performance_issue}}\n\n**SQL Query:**\n```sql\n{{sql_query}}\n```\n\nAnalyze the query, point out potential full table scans or inefficient joins, and rewrite the query for optimal performance. Suggest any indexes that should be created.',
  E'You are a master of SQL execution plans and indexing strategies.',
  E'Output will be an optimized query and indexing recommendations.',
  '{"en": "Paste your slow query and mention the DB engine.", "vi": "Dán truy vấn chậm của bạn và đề cập đến engine CSDL."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T10
(
  'd0000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000011', 1,
  E'Act as a Product Marketing Manager. Generate user-facing release notes.\n\n**Version Number:** {{version_number}}\n\n**New Features:**\n{{new_features}}\n\n**Bug Fixes:**\n{{bug_fixes}}\n\nFormat the notes in Markdown. Use an enthusiastic tone for the new features, focusing on the user benefit rather than technical implementation. Group the bug fixes cleanly under a "Improvements & Fixes" section.',
  E'You translate technical jargon into clear, exciting updates for end users.',
  E'Output will be nicely formatted release notes ready for publishing.',
  '{"en": "Input your changelog to get polished release notes.", "vi": "Nhập nhật ký thay đổi của bạn để có ghi chú phát hành hoàn chỉnh."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T11
(
  'd0000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000012', 1,
  E'**Role:**\n{{role}}\n\n**Context:**\n{{context}}\n\n**Task:**\n{{task}}\n\n**Requirements:**\n{{requirements}}\n\n**Constraints:**\n{{constraints}}\n\n**Example:**\n{{example}}\n\n**Additional Info:**\n{{additional_info}}',
  E'You are a helpful, versatile AI assistant.',
  E'Output aligned with the requested task and requirements.',
  '{"en": "Fill in the framework details.", "vi": "Điền các chi tiết vào khung."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
),
-- T12
(
  'd0000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000013', 1,
  E'**Context:**\n{{context}}\n\n**Task:**\n{{task}}\n\n**Output Format:**\n{{output_format}}',
  E'You are a fast and efficient AI assistant.',
  E'Direct and concise output matching the requested format.',
  '{"en": "Provide a quick task.", "vi": "Cung cấp một tác vụ nhanh."}', true, '11111111-1111-1111-1111-111111111111', 'Initial release'
)
ON CONFLICT (id) DO NOTHING;

-- =============================================================
-- 3. TEMPLATE VARIABLES
-- =============================================================
INSERT INTO template_variables (
  id, template_version_id, var_key, label_i18n, description_i18n,
  placeholder_i18n, help_text_i18n, input_type, is_required,
  default_value, options, validation, sort_order
) VALUES
-- T1 Vars: feature_description, input_parameters, expected_outcomes
('e0000000-0000-0000-0001-000000000002', 'd0000000-0000-0000-0000-000000000002', 'feature_description', '{"en": "Feature Description", "vi": "Mô tả Tính năng"}', '{}', '{"en": "e.g., A user login form", "vi": "VD: Form đăng nhập người dùng"}', '{}', 'textarea', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000002', 'd0000000-0000-0000-0000-000000000002', 'input_parameters', '{"en": "Input Parameters", "vi": "Các Tham số Đầu vào"}', '{}', '{"en": "e.g., Username (5-15 chars), Password", "vi": "VD: Tên đăng nhập (5-15 ký tự), Mật khẩu"}', '{}', 'textarea', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000002', 'd0000000-0000-0000-0000-000000000002', 'expected_outcomes', '{"en": "Expected Outcomes", "vi": "Kết quả Mong đợi"}', '{}', '{"en": "e.g., Redirect to dashboard on success, show error on failure", "vi": "VD: Chuyển đến dashboard khi thành công, hiện lỗi khi thất bại"}', '{}', 'textarea', true, NULL, '[]', '{}', 2),
-- T2 Vars: script_code, performance_bottlenecks, unity_version
('e0000000-0000-0000-0001-000000000003', 'd0000000-0000-0000-0000-000000000003', 'script_code', '{"en": "C# Script Code", "vi": "Mã C#"}', '{}', '{"en": "Paste your code here", "vi": "Dán mã của bạn vào đây"}', '{}', 'textarea', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000003', 'd0000000-0000-0000-0000-000000000003', 'performance_bottlenecks', '{"en": "Performance Bottlenecks", "vi": "Các Nút thắt Hiệu suất"}', '{}', '{"en": "e.g., Frame drops during enemy spawn", "vi": "VD: Sụt khung hình khi sinh ra kẻ thù"}', '{}', 'textarea', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000003', 'd0000000-0000-0000-0000-000000000003', 'unity_version', '{"en": "Unity Version", "vi": "Phiên bản Unity"}', '{}', '{"en": "e.g., 2022.3 LTS", "vi": "VD: 2022.3 LTS"}', '{}', 'text', true, NULL, '[]', '{}', 2),
-- T3 Vars: character_name, background_story, current_conflict
('e0000000-0000-0000-0001-000000000004', 'd0000000-0000-0000-0000-000000000004', 'character_name', '{"en": "Character Name", "vi": "Tên Nhân vật"}', '{}', '{"en": "e.g., Kenji", "vi": "VD: Kenji"}', '{}', 'text', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000004', 'd0000000-0000-0000-0000-000000000004', 'background_story', '{"en": "Background Story", "vi": "Câu chuyện Bối cảnh"}', '{}', '{"en": "e.g., Grew up in a ruined city", "vi": "VD: Lớn lên trong một thành phố hoang tàn"}', '{}', 'textarea', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000004', 'd0000000-0000-0000-0000-000000000004', 'current_conflict', '{"en": "Current Conflict", "vi": "Xung đột Hiện tại"}', '{}', '{"en": "e.g., Must choose between saving a friend or stopping the antagonist.", "vi": "VD: Phải chọn giữa cứu bạn hay ngăn chặn kẻ phản diện."}', '{}', 'textarea', true, NULL, '[]', '{}', 2),
-- T4 Vars: job_title, department, key_responsibilities, qualifications
('e0000000-0000-0000-0001-000000000005', 'd0000000-0000-0000-0000-000000000005', 'job_title', '{"en": "Job Title", "vi": "Chức danh Công việc"}', '{}', '{"en": "e.g., Senior Frontend Engineer", "vi": "VD: Kỹ sư Frontend Cao cấp"}', '{}', 'text', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000005', 'd0000000-0000-0000-0000-000000000005', 'department', '{"en": "Department", "vi": "Phòng ban"}', '{}', '{"en": "e.g., Product Development", "vi": "VD: Phát triển Sản phẩm"}', '{}', 'text', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000005', 'd0000000-0000-0000-0000-000000000005', 'key_responsibilities', '{"en": "Key Responsibilities", "vi": "Trách nhiệm Chính"}', '{}', '{"en": "e.g., Build UI components", "vi": "VD: Xây dựng UI component"}', '{}', 'textarea', true, NULL, '[]', '{}', 2),
('e0000000-0000-0000-0004-000000000005', 'd0000000-0000-0000-0000-000000000005', 'qualifications', '{"en": "Required Qualifications", "vi": "Yêu cầu Bằng cấp/Kinh nghiệm"}', '{}', '{"en": "e.g., 5+ years React experience", "vi": "VD: Hơn 5 năm kinh nghiệm React"}', '{}', 'textarea', true, NULL, '[]', '{}', 3),
-- T5 Vars: target_industry, value_proposition, pain_points
('e0000000-0000-0000-0001-000000000006', 'd0000000-0000-0000-0000-000000000006', 'target_industry', '{"en": "Target Industry", "vi": "Ngành Mục tiêu"}', '{}', '{"en": "e.g., Mid-sized Tech Companies", "vi": "VD: Công ty Công nghệ Cỡ trung"}', '{}', 'text', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000006', 'd0000000-0000-0000-0000-000000000006', 'value_proposition', '{"en": "Value Proposition", "vi": "Tuyên bố Giá trị"}', '{}', '{"en": "e.g., Automate payroll", "vi": "VD: Tự động hóa bảng lương"}', '{}', 'textarea', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000006', 'd0000000-0000-0000-0000-000000000006', 'pain_points', '{"en": "Pain Points", "vi": "Điểm Đau (Vấn đề)"}', '{}', '{"en": "e.g., Manual data entry errors", "vi": "VD: Lỗi nhập liệu thủ công"}', '{}', 'textarea', true, NULL, '[]', '{}', 2),
-- T6 Vars: primary_keyword, secondary_keywords, target_audience
('e0000000-0000-0000-0001-000000000007', 'd0000000-0000-0000-0000-000000000007', 'primary_keyword', '{"en": "Primary Keyword", "vi": "Từ khóa Chính"}', '{}', '{"en": "e.g., remote work tools 2026", "vi": "VD: công cụ làm việc từ xa 2026"}', '{}', 'text', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000007', 'd0000000-0000-0000-0000-000000000007', 'secondary_keywords', '{"en": "Secondary Keywords", "vi": "Từ khóa Phụ"}', '{}', '{"en": "e.g., wfh software", "vi": "VD: phần mềm wfh"}', '{}', 'text', false, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000007', 'd0000000-0000-0000-0000-000000000007', 'target_audience', '{"en": "Target Audience", "vi": "Đối tượng Mục tiêu"}', '{}', '{"en": "e.g., IT Professionals", "vi": "VD: Chuyên gia IT"}', '{}', 'text', true, NULL, '[]', '{}', 2),
-- T7 Vars: component_code, target_react_version, specific_issues
('e0000000-0000-0000-0001-000000000008', 'd0000000-0000-0000-0000-000000000008', 'component_code', '{"en": "Component Code", "vi": "Mã Component"}', '{}', '{"en": "Paste React code here", "vi": "Dán mã React vào đây"}', '{}', 'textarea', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000008', 'd0000000-0000-0000-0000-000000000008', 'target_react_version', '{"en": "Target React Version", "vi": "Phiên bản React Đích"}', '{}', '{"en": "e.g., React 18", "vi": "VD: React 18"}', '{}', 'text', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000008', 'd0000000-0000-0000-0000-000000000008', 'specific_issues', '{"en": "Specific Issues", "vi": "Vấn đề Cụ thể"}', '{}', '{"en": "e.g., Unnecessary re-renders", "vi": "VD: Re-render không cần thiết"}', '{}', 'textarea', true, NULL, '[]', '{}', 2),
-- T8 Vars: product_name, problem_solved, target_market, business_model
('e0000000-0000-0000-0001-000000000009', 'd0000000-0000-0000-0000-000000000009', 'product_name', '{"en": "Product Name", "vi": "Tên Sản phẩm"}', '{}', '{"en": "e.g., LearnAI", "vi": "VD: LearnAI"}', '{}', 'text', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000009', 'd0000000-0000-0000-0000-000000000009', 'problem_solved', '{"en": "Problem Solved", "vi": "Vấn đề Được giải quyết"}', '{}', '{"en": "e.g., Education is failing students", "vi": "VD: Giáo dục đang làm sinh viên thất bại"}', '{}', 'textarea', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000009', 'd0000000-0000-0000-0000-000000000009', 'target_market', '{"en": "Target Market", "vi": "Thị trường Mục tiêu"}', '{}', '{"en": "e.g., University students globally", "vi": "VD: Sinh viên đại học toàn cầu"}', '{}', 'text', true, NULL, '[]', '{}', 2),
('e0000000-0000-0000-0004-000000000009', 'd0000000-0000-0000-0000-000000000009', 'business_model', '{"en": "Business Model", "vi": "Mô hình Kinh doanh"}', '{}', '{"en": "e.g., B2C Subscription $10/month", "vi": "VD: Đăng ký B2C 10$/tháng"}', '{}', 'textarea', true, NULL, '[]', '{}', 3),
-- T9 Vars: sql_query, database_engine, performance_issue
('e0000000-0000-0000-0001-000000000010', 'd0000000-0000-0000-0000-000000000010', 'sql_query', '{"en": "SQL Query", "vi": "Truy vấn SQL"}', '{}', '{"en": "Paste your slow query here", "vi": "Dán truy vấn chậm vào đây"}', '{}', 'textarea', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000010', 'd0000000-0000-0000-0000-000000000010', 'database_engine', '{"en": "Database Engine", "vi": "Hệ Quản trị CSDL"}', '{}', '{"en": "e.g., PostgreSQL 15", "vi": "VD: PostgreSQL 15"}', '{}', 'text', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000010', 'd0000000-0000-0000-0000-000000000010', 'performance_issue', '{"en": "Performance Issue", "vi": "Vấn đề Hiệu suất"}', '{}', '{"en": "e.g., Takes 5 seconds to run", "vi": "VD: Mất 5 giây để chạy"}', '{}', 'textarea', true, NULL, '[]', '{}', 2),
-- T10 Vars: version_number, new_features, bug_fixes
('e0000000-0000-0000-0001-000000000011', 'd0000000-0000-0000-0000-000000000011', 'version_number', '{"en": "Version Number", "vi": "Số Phiên bản"}', '{}', '{"en": "e.g., v2.1.0", "vi": "VD: v2.1.0"}', '{}', 'text', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000011', 'd0000000-0000-0000-0000-000000000011', 'new_features', '{"en": "New Features", "vi": "Tính năng Mới"}', '{}', '{"en": "e.g., Added Dark Mode", "vi": "VD: Thêm Chế độ Tối"}', '{}', 'textarea', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000011', 'd0000000-0000-0000-0000-000000000011', 'bug_fixes', '{"en": "Bug Fixes", "vi": "Các Lỗi Đã sửa"}', '{}', '{"en": "e.g., Fixed crash on login", "vi": "VD: Sửa lỗi văng ứng dụng"}', '{}', 'textarea', true, NULL, '[]', '{}', 2),
-- T11 Vars: role, context, task, requirements, constraints, example, additional_info
('e0000000-0000-0000-0001-000000000012', 'd0000000-0000-0000-0000-000000000012', 'role', '{"en": "Role", "vi": "Vai trò"}', '{}', '{"en": "e.g., Expert Marketer", "vi": "VD: Chuyên gia Tiếp thị"}', '{}', 'text', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000012', 'd0000000-0000-0000-0000-000000000012', 'context', '{"en": "Context", "vi": "Bối cảnh"}', '{}', '{"en": "e.g., We are launching a new product", "vi": "VD: Chúng tôi đang ra mắt sản phẩm mới"}', '{}', 'textarea', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000012', 'd0000000-0000-0000-0000-000000000012', 'task', '{"en": "Task", "vi": "Nhiệm vụ"}', '{}', '{"en": "e.g., Write an announcement post", "vi": "VD: Viết bài thông báo"}', '{}', 'textarea', true, NULL, '[]', '{}', 2),
('e0000000-0000-0000-0004-000000000012', 'd0000000-0000-0000-0000-000000000012', 'requirements', '{"en": "Requirements", "vi": "Yêu cầu"}', '{}', '{"en": "e.g., Must be engaging", "vi": "VD: Phải hấp dẫn"}', '{}', 'textarea', true, NULL, '[]', '{}', 3),
('e0000000-0000-0000-0005-000000000012', 'd0000000-0000-0000-0000-000000000012', 'constraints', '{"en": "Constraints", "vi": "Ràng buộc"}', '{}', '{"en": "e.g., Max 200 words", "vi": "VD: Tối đa 200 từ"}', '{}', 'textarea', true, NULL, '[]', '{}', 4),
('e0000000-0000-0000-0006-000000000012', 'd0000000-0000-0000-0000-000000000012', 'example', '{"en": "Example", "vi": "Ví dụ"}', '{}', '{"en": "e.g., Previous post", "vi": "VD: Bài đăng trước"}', '{}', 'textarea', false, NULL, '[]', '{}', 5),
('e0000000-0000-0000-0007-000000000012', 'd0000000-0000-0000-0000-000000000012', 'additional_info', '{"en": "Additional Info", "vi": "Thông tin Bổ sung"}', '{}', '{"en": "Any other context", "vi": "Bất kỳ bối cảnh nào khác"}', '{}', 'textarea', false, NULL, '[]', '{}', 6),
-- T12 Vars: task, context, output_format
('e0000000-0000-0000-0001-000000000013', 'd0000000-0000-0000-0000-000000000013', 'task', '{"en": "Task", "vi": "Nhiệm vụ"}', '{}', '{"en": "e.g., Summarize this", "vi": "VD: Tóm tắt cái này"}', '{}', 'textarea', true, NULL, '[]', '{}', 0),
('e0000000-0000-0000-0002-000000000013', 'd0000000-0000-0000-0000-000000000013', 'context', '{"en": "Context", "vi": "Bối cảnh"}', '{}', '{"en": "e.g., Long email thread", "vi": "VD: Chuỗi email dài"}', '{}', 'textarea', true, NULL, '[]', '{}', 1),
('e0000000-0000-0000-0003-000000000013', 'd0000000-0000-0000-0000-000000000013', 'output_format', '{"en": "Output Format", "vi": "Định dạng Đầu ra"}', '{}', '{"en": "e.g., Bullet points", "vi": "VD: Các gạch đầu dòng"}', '{}', 'text', true, NULL, '[]', '{}', 2)
ON CONFLICT (id) DO NOTHING;

-- =============================================================
-- 4. UPDATE CURRENT VERSION ID FOR NEW TEMPLATES
-- =============================================================
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000002' WHERE id = 'c0000000-0000-0000-0000-000000000002';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000003' WHERE id = 'c0000000-0000-0000-0000-000000000003';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000004' WHERE id = 'c0000000-0000-0000-0000-000000000004';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000005' WHERE id = 'c0000000-0000-0000-0000-000000000005';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000006' WHERE id = 'c0000000-0000-0000-0000-000000000006';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000007' WHERE id = 'c0000000-0000-0000-0000-000000000007';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000008' WHERE id = 'c0000000-0000-0000-0000-000000000008';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000009' WHERE id = 'c0000000-0000-0000-0000-000000000009';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000010' WHERE id = 'c0000000-0000-0000-0000-000000000010';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000011' WHERE id = 'c0000000-0000-0000-0000-000000000011';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000012' WHERE id = 'c0000000-0000-0000-0000-000000000012';
UPDATE templates SET current_version_id = 'd0000000-0000-0000-0000-000000000013' WHERE id = 'c0000000-0000-0000-0000-000000000013';

-- =============================================================
-- 5. GENERATED PROMPTS (5 Prompts)
-- =============================================================
INSERT INTO generated_prompts (
  id, user_id, workspace_id, template_id, template_version_id,
  ai_model_id, title, input_values, extra_instructions, final_prompt
) VALUES
-- P1: Test Case for Login Form
(
  'f0000000-0000-0000-0000-000000000003', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000002', NULL,
  'Login Form Test Cases',
  '{"feature_description": "User login form on the main landing page", "input_parameters": "Username (must be 5 to 15 characters, alphanumeric only), Password (minimum 8 characters, at least 1 number and 1 special character)", "expected_outcomes": "Successful login redirects to dashboard. Failed login shows appropriate inline error message."}',
  NULL,
  E'Act as a Senior QA Automation Engineer. I need comprehensive black-box test cases for the following feature.\n\n**Feature Description:**\nUser login form on the main landing page\n\n**Input Parameters:**\nUsername (must be 5 to 15 characters, alphanumeric only), Password (minimum 8 characters, at least 1 number and 1 special character)\n\n**Expected Outcomes:**\nSuccessful login redirects to dashboard. Failed login shows appropriate inline error message.\n\nPlease generate a test suite applying Boundary Value Analysis (BVA) and Equivalence Class Partitioning (ECP). Format the output as a Markdown table with columns: Test Case ID, Description, Technique (BVA/ECP), Input Data, Expected Result.'
),
-- P2: Tower Defense Spawn Optimization
(
  'f0000000-0000-0000-0000-000000000004', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  'c0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000003', NULL,
  'Tower Defense Spawn Optimization',
  '{"unity_version": "2022.3 LTS", "performance_bottlenecks": "Heavy GC spikes and frame drops when spawning large waves of enemies.", "script_code": "void Update() { if(shouldSpawn) { Instantiate(enemyPrefab, spawnPoint.position, Quaternion.identity); } }"}',
  NULL,
  E'Act as a Unity Performance Optimization Expert. I have a script causing frame drops.\n\n**Unity Version:** 2022.3 LTS\n\n**Performance Bottlenecks Noticed:**\nHeavy GC spikes and frame drops when spawning large waves of enemies.\n\n**Script Code:**\n```csharp\nvoid Update() { if(shouldSpawn) { Instantiate(enemyPrefab, spawnPoint.position, Quaternion.identity); } }\n```\n\nPlease refactor the code to eliminate GC allocations, avoid expensive calls in Update(), and use object pooling if necessary.'
),
-- P3: Outreach to HR Directors
(
  'f0000000-0000-0000-0000-000000000005', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  'c0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000006', NULL,
  'Outreach to HR Directors',
  '{"target_industry": "HR Directors at mid-sized tech companies", "pain_points": "Spending too much time manually consolidating payroll data", "value_proposition": "Our automated payroll software integrates with existing HRIS tools"}',
  NULL,
  E'Act as a B2B SaaS Sales Executive. Write a cold outreach email.\n\n**Target Industry:** HR Directors at mid-sized tech companies\n**Pain Points:** Spending too much time manually consolidating payroll data\n**Our Value Proposition:** Our automated payroll software integrates with existing HRIS tools\n\nThe email must have a catchy subject line, a personalized hook, directly address the pain point, present our value proposition softly, and end with a low-friction call to action (e.g., a quick 10-minute chat). Keep it under 150 words.'
),
-- P4: Future of Remote Work Blog
(
  'f0000000-0000-0000-0000-000000000006', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  'c0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000007', NULL,
  'Future of Remote Work Blog Outline',
  '{"target_audience": "IT Professionals and Engineering Managers", "primary_keyword": "remote work tools 2026", "secondary_keywords": "asynchronous collaboration"}',
  NULL,
  E'Act as an SEO Content Strategist. I need a blog post outline.\n\n**Target Audience:** IT Professionals and Engineering Managers\n**Primary Keyword:** remote work tools 2026\n**Secondary Keywords:** asynchronous collaboration\n\nProvide an outline including a working title, H2 and H3 headings, where to naturally insert the keywords, and a bulleted list of key points to cover in each section. Suggest an engaging hook for the introduction.'
),
-- P5: AI Learning Platform Pitch
(
  'f0000000-0000-0000-0000-000000000007', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  'c0000000-0000-0000-0000-000000000009', 'd0000000-0000-0000-0000-000000000009', NULL,
  'AI Learning Platform Pitch Deck',
  '{"product_name": "LearnAI", "target_market": "University students globally", "problem_solved": "One-size-fits-all education leaves students struggling", "business_model": "B2C Subscription at $10/month"}',
  NULL,
  E'Act as a Venture Capitalist and Pitch Coach. I need a 10-slide pitch deck storyboard.\n\n**Product Name:** LearnAI\n**Target Market:** University students globally\n**Problem Solved:** One-size-fits-all education leaves students struggling\n**Business Model:** B2C Subscription at $10/month\n\nFor each slide, provide a Title, Key Message (1 sentence), and Visual Suggestion (what the slide should show). Focus on narrative flow and investor priorities (market size, traction, unit economics).'
)
ON CONFLICT (id) DO NOTHING;
