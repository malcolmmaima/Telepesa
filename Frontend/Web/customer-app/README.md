# Telepesa Customer Web App

Minimal, secure React + TypeScript app for customer-facing banking.

## Setup

1. Ensure Node.js 18+ installed
2. Copy environment variables:

```bash
cp .env.example .env
```

3. Configure API endpoint in `.env`:

```bash
# For development with backend services running:
VITE_API_BASE_URL=http://localhost:8082/api/v1

# The frontend runs on http://localhost:5173
# Backend services typically run on:
# - Account Service: 8082
# - Transaction Service: 8083
# - User Service: 8081
```

## Scripts

```bash
npm install
npm run dev    # http://localhost:5173
npm run build
npm run preview
```

## API Configuration

The app connects to backend microservices:

- **Account Service** (port 8082): Account management, balances
- **User Service** (port 8081): Authentication, user profiles
- **Transaction Service** (port 8083): Transaction history, transfers

### Environment Variables

- `VITE_API_BASE_URL`: Backend API base URL (default: `http://localhost:8082/api/v1`)
- `VITE_API_MODE`: Set to `mock` for development without backend services

### Development Without Backend

If backend services aren't running, the app gracefully degrades:

- Shows empty states instead of errors
- Logs helpful messages to console
- Core UI functionality remains intact

## Notes

- **Authentication**: JWT with refresh token rotation in `src/api/client.ts`
- **Styling**: Tailwind CSS with custom financial color palette
- **Testing**: Vitest + React Testing Library
- **Error Handling**: Graceful degradation when APIs unavailable
