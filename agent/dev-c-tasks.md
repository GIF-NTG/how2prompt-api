# Tasks: Dev C - Identity Module: Login & Token Lifecycle

**Related Plan**: [dev-c-plan.md](./dev-c-plan.md)
**User Stories**: US-1.1, US-1.4

## Phase 1: Setup

- [X] T001 Verify project structure and `common` dependencies exist in `how2prompt-api/src/main/java/com/example/how2prompt/`

## Phase 2: Foundational (Entities & Repositories)

- [X] T002 [P] Create `UserIdentity` entity in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/entity/UserIdentity.java`
- [X] T003 [P] Create `RefreshToken` entity in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/entity/RefreshToken.java`
- [X] T004 Create `UserIdentityRepository` in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/repository/UserIdentityRepository.java`
- [X] T005 Create `RefreshTokenRepository` in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/repository/RefreshTokenRepository.java`

## Phase 3: Token Management Services [US-1.1] [US-1.4]

- [X] T006 [US-1.1] Create `RefreshTokenService` (hash compare, revoke logic) in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/service/RefreshTokenService.java`
- [X] T007 [US-1.1] Create `AuthService` (verify password, issue tokens) in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/service/AuthService.java`
- [X] T008 [P] [US-1.1] Create `LoginRequest` and token response DTOs in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/dto/`

## Phase 4: Authentication Endpoints [US-1.1] [US-1.4]

- [X] T009 [US-1.1] Implement `POST /api/v1/auth/login` endpoint in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/controller/AuthController.java`
- [X] T010 [US-1.4] Implement `POST /api/v1/auth/refresh` endpoint in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/controller/AuthController.java`
- [X] T011 [US-1.4] Implement `POST /api/v1/auth/logout` endpoint in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/controller/AuthController.java`

## Phase 5: Polish & Testing

- [X] T012 Verify `workspace_id` validation logic integration and `deleted_at` filters across queries in `how2prompt-api/src/main/java/com/example/how2prompt/modules/identity/service/AuthService.java`
- [X] T013 Create `.http` testing file for auth flow (register, login, refresh, logout) in `how2prompt-api/agent/auth-flow.http`

## Dependencies

- **T002, T003** are parallelizable and MUST be completed before Repositories (T004, T005).
- **T004, T005** MUST be completed before Services (T006, T007).
- **T006, T007** MUST be completed before Endpoints (T009, T010, T011).
- Endpoints (T009-T011) can be implemented sequentially in `AuthController.java`.
