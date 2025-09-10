## Security Implementation

### Authentication & Authorization
This system uses JWT-based stateless authentication with short-lived access tokens.

#### New: Refresh Tokens and Rotation
- Access tokens are short-lived and included in the `Authorization: Bearer <accessToken>` header.
- A long-lived `refreshToken` is issued on login and stored server-side (DB) with expiry, `revoked`, and `replacedByToken` fields.
- Refresh endpoint: `POST /api/v1/users/refresh` (via API Gateway).
- On refresh, the presented refresh token is validated, then rotated:
  - Current token is marked `revoked=true` and its `replacedByToken` is set.
  - A brand new `refreshToken` is issued and persisted.
  - A new `accessToken` is returned.
- This mitigates replay using stolen refresh tokens and supports token compromise containment.

Client guidance:
- Store `accessToken` in memory; store `refreshToken` in secure, http-only storage appropriate to the client platform.
- Always call refresh through the API Gateway, not the service directly.
- Handle 401 during refresh by logging out the user.

### API Gateway Routing (Required)
All external API calls must traverse the API Gateway at `http://localhost:8080`.

Key open routes related to auth:
- `POST /api/v1/users/register`
- `POST /api/v1/users/login`
- `POST /api/v1/users/refresh`

All other `/api/v1/users/**` routes require a valid `accessToken`.
 