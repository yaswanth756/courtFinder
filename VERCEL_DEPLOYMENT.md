# CourtFinder backend deployment on Vercel

The backend is packaged by `Dockerfile.vercel` as a Kotlin/Ktor HTTP service. Vercel must use its Container runtime (or detect the `Dockerfile.vercel` entrypoint).

## Required production environment variables

- `COURTFINDER_ENV=production`
- `COURTFINDER_JWT_SECRET`: a randomly generated secret with at least 32 characters
- `DATABASE_URL`: durable PostgreSQL connection URI (`postgres://` or `postgresql://`); TLS is required by default
- `COURTFINDER_FIREBASE_PROJECT_ID=courtfinder-e36ea`
- `COURTFINDER_FIREBASE_SERVICE_ACCOUNT_JSON`: Firebase Admin service-account JSON stored as a secret environment variable, never in Git or the Android app
- `COURTFINDER_GOOGLE_WEB_CLIENT_ID`: the OAuth Web client ID used by the account-deletion web flow

Vercel provides `PORT`; the service listens on it. For local development, omit `DATABASE_URL` and use SQLite with `COURTFINDER_ENV=development`.

## Before production traffic

1. Provision PostgreSQL and add its connection URI to Vercel as `DATABASE_URL`.
2. Migrate any required records from the ignored local `backend/data/courtfinder.db` using a reviewed export/import. The service does not automatically copy data between SQLite and PostgreSQL.
3. Add the environment variables above in Vercel's project settings.
4. Deploy and check `GET /api/health`.
5. Load verified real court/availability data into the production database. Development demo courts are intentionally not seeded in production.
6. Update the Android release `API_BASE_URL` to the deployed HTTPS origin and verify registration, login, Google sign-in, search, booking, and account deletion.

Do not set `COURTFINDER_ENV=development` in production: it enables a local development JWT fallback and seeds fictional demo courts.
