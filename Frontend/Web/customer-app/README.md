# Telepesa Customer Web App

Minimal, secure React + TypeScript app for customer-facing banking.

## Setup

- Ensure Node.js 18+ installed
- Copy env

```bash
# set VITE_API_BASE if needed (defaults to http://localhost:8080/api/v1)
```

## Scripts

```bash
npm install
npm run dev    # http://localhost:5173
npm run build
npm run preview
```

## Notes
- All APIs via API Gateway `VITE_API_BASE` (default `http://localhost:8080/api/v1`).
- Auth: accessToken + refreshToken rotation handled in `src/api/client.ts`.
- Styles: minimal utility classes in `src/styles.css` with financial palette.
