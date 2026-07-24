# Dev C Implementation Plan - Identity Module: Login & Token Lifecycle

**Role:** Dev C
**Focus:** Authentication flow, token generation, refresh tokens, and identity mapping.
**Related User Stories:** US-1.1, US-1.4

## 1. Technical Context & Constraints
- **Dependencies:** Relies on `common/response` and `common/security` from Dev A.
- **Security:** Do not trust `workspace_id` from the client; always derive it from the `SecurityContext`.
- **Statelessness:** The system uses stateless JWT tokens for API calls.
- **Refresh Token Storage:** Save only the `token_hash` in the database, never the plain refresh token.
- **Cookie Security:** Refresh tokens must be set via `httpOnly`, `Secure`, `SameSite=Strict` cookies.

## 2. Data Model (data-model.md)

### `UserIdentity` Entity
Used to map users for future OAuth integrations.
- `id` (UUID, Primary Key)
- `user_id` (UUID, Foreign Key to `User`)
- `provider` (String, e.g., "LOCAL", "GOOGLE", "GITHUB")
- `provider_id` (String, e.g., the user's Google ID)
- `created_at` (Timestamp)
- `updated_at` (Timestamp)
- `deleted_at` (Timestamp, for soft deletes)

### `RefreshToken` Entity
Stores hashed refresh tokens for secure validation and revocation.
- `id` (UUID, Primary Key)
- `user_id` (UUID, Foreign Key to `User`)
- `token_hash` (String, hashed value of the plain refresh token)
- `expires_at` (Timestamp, 30 days validity)
- `revoked_at` (Timestamp, nullable. If set, token is invalid)
- `created_at` (Timestamp)
- `updated_at` (Timestamp)

## 3. Contracts (/contracts/)

### 3.1. `POST /api/v1/auth/login`
- **Request:**
  ```json
  {
    "email": "user@example.com",
    "password": "SecurePassword123!"
  }
  ```
- **Response (200 OK):**
  - **Headers:** `Set-Cookie: refresh_token=...; HttpOnly; Secure; SameSite=Strict; Max-Age=2592000`
  - **Body:**
    ```json
    {
      "data": {
        "access_token": "eyJhbGciOiJSUzI1Ni... (15 mins validity)",
        "expires_in": 900
      }
    }
    ```

### 3.2. `POST /api/v1/auth/refresh`
- **Request:**
  - **Headers:** `Cookie: refresh_token=...`
- **Response (200 OK):**
  - **Body:**
    ```json
    {
      "data": {
        "access_token": "eyJhbGciOiJSUzI1Ni...",
        "expires_in": 900
      }
    }
    ```
- **Error (401 Unauthorized):** standard RFC-7807 if refresh token is invalid/revoked/expired.

### 3.3. `POST /api/v1/auth/logout`
- **Request:**
  - **Headers:** `Authorization: Bearer <access_token>`, `Cookie: refresh_token=...`
- **Response (204 No Content):**
  - **Headers:** `Set-Cookie: refresh_token=; HttpOnly; Secure; SameSite=Strict; Max-Age=0`

## 4. Quickstart / Testing Guide (quickstart.md)

1. **Setup Postman Collection:**
   - Create a Postman environment with variables `{{base_url}}`, `{{access_token}}`.
2. **Test Login:**
   - Send `POST /api/v1/auth/login` with valid credentials.
   - Assert `access_token` is returned in response body.
   - Assert `refresh_token` cookie is set.
3. **Test Protected Route (Wait for Dev A / me endpoint):**
   - Send `GET /api/v1/users/me` using the `access_token`.
4. **Test Token Refresh:**
   - Send `POST /api/v1/auth/refresh` ensuring the `refresh_token` cookie is included.
   - Assert a new `access_token` is received.
5. **Test Logout:**
   - Send `POST /api/v1/auth/logout`.
   - Ensure the token hash is marked as revoked in the database.
   - Ensure the cookie is cleared in the response headers.
