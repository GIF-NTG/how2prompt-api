** 🧑‍💻 Dev A — Project Foundation & DevOps (người dựng nền, nên là người cứng nhất về Spring config)

Ngày 1–2:



Khởi tạo Spring Boot 3 project, đúng cấu trúc config/, common/, infrastructure/, modules/ theo mục 1 trong agent instructions.

docker-compose.yml (Postgres 15 + Redis 7).

Setup Flyway

Setup springdoc-openapi tại /swagger-ui.

Setup common/response (ApiResponse<T>, PageResponse<T>) và common/exception (GlobalExceptionHandler, format lỗi chuẩn {error:{code,message,details}}) — 2 dev còn lại phụ thuộc vào cái này nên phải xong sớm, ưu tiên số 1.

Ngày 3–4 → hết tuần:



infrastructure/security: JwtTokenProvider (RS256, access 15p/refresh 30 ngày), JwtAuthFilter, cấu hình Spring Security filter chain stateless.

common/security: @CurrentUser annotation + AuthenticatedUser — nền tảng để Dev B/C lấy user_id/workspace_id từ SecurityContext (bắt buộc theo mục 5 — không tin request body).

Support 2 dev kia review integration, chạy thử .http/Postman collection cuối tuần.

🧑‍💻 Dev B — Identity Module: Register & Workspace (US-1.1, US-1.5)

Ngày 3–4 (bắt đầu sau khi Dev A có entity base + BaseEntity):



Entity trong modules/identity/entity/: User, Workspace, WorkspaceMember (chú ý @Id UUID do DB tự sinh, @SQLRestriction("deleted_at IS NULL")).

PasswordEncoder BCrypt cost ≥ 12.

Ngày 5:



POST /auth/register: tạo user + hash password + transaction tạo kèm personal workspace (type='personal') + workspace_member (role='owner') — 1 AuthService.register() xử lý toàn bộ trong 1 @Transactional.

Validate email format + password strength (RegisterRequest DTO + @Valid).

Tích hợp gửi email verify (SendGrid/Resend) — làm @Async, không block response.

Integration test: register → workspace tự tạo đúng role owner.

Ngày 7:



GET /users/me, PATCH /users/me (full_name, avatar, bio, locale, timezone).

🧑‍💻 Dev C — Identity Module: Login & Token Lifecycle (US-1.1, US-1.4)

Ngày 3–4 (song song Dev B, khác entity nên ít đụng nhau):



Entity UserIdentity (dùng cho OAuth sau này, nhưng map trước) + entity refresh_tokens (lưu token_hash, không lưu plain token).

Phối hợp với Dev A về format JWT claims cần thiết (user_id, workspace_id mặc định).

Ngày 6:



POST /auth/login: verify password, trả access token + set refresh token qua httpOnly cookie.

POST /auth/refresh: verify refresh token còn hạn + chưa revoke, cấp access token mới.

POST /auth/logout: revoke refresh token hiện tại.

RefreshTokenRepository + RefreshTokenService (revoke logic, hash compare).

Ngày 7:



Viết Postman collection / .http file cho toàn bộ luồng auth (register/login/refresh/logout/me) để bàn giao FE.

Cùng cả team review lại checklist mục 9 (đặc biệt: workspace_id không tin từ client, mọi entity có deleted_at filter đúng).

Lưu ý phối hợp quan trọng

Vấn đềGiải phápDev B và Dev C cùng động vào modules/identityChia rõ theo entity: Dev B sở hữu User/Workspace/WorkspaceMember, Dev C sở hữuUserIdentity/RefreshToken. Tránh 2 người sửa cùng 1 file entity.Cả 2 phụ thuộc common/response và common/security của Dev ADev A phải merge phần này trước trưa ngày 2 để không block.JWT claims cần thống nhất sớmDev A + Dev C họp nhanh ngày 3 để chốt claims JWT chứa gì (user_id, default workspace_id, role...).Cuối tuần bàn giao FE/auth/register, /auth/login, /auth/refresh,/users/me — Dev C tổng hợp Postman, cả 3 cùng smoke-test trước khi gửi FE. 

