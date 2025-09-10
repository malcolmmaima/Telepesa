/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_APP_NAME: string
  readonly VITE_APP_VERSION: string
  readonly VITE_APP_ENVIRONMENT: string
  readonly VITE_ENABLE_LOANS: string
  readonly VITE_ENABLE_INVESTMENTS: string
  readonly VITE_ENABLE_ANALYTICS: string
  readonly VITE_GOOGLE_ANALYTICS_ID: string
  readonly VITE_SENTRY_DSN: string
  readonly VITE_DEV_TOOLS: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
