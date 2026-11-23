# CourtFinder Next.js Backend Deployment on Vercel

The backend has been converted to a **Next.js (App Router)** TypeScript application specifically designed for native, zero-configuration deployment on Vercel Serverless Functions.

## 1. Vercel Project Configuration

When importing or creating the project on [Vercel](https://vercel.com):

1. **Root Directory**: Set to `backend` (Vercel Dashboard → Project Settings → General → Root Directory → `backend`).
2. **Framework Preset**: `Next.js` (auto-detected).
3. **Build Command**: `next build` (default).
4. **Output Directory**: `.next` (default).

---

## 2. Required Production Environment Variables

Add the following environment variables in **Vercel Settings → Environment Variables**:

| Variable | Description | Example / Note |
| :--- | :--- | :--- |
| `DATABASE_URL` | PostgreSQL connection string | `postgres://user:pass@ep-xyz.neon.tech/neondb?sslmode=require` |
| `COURTFINDER_JWT_SECRET` | Secret key for signing session JWT tokens | Minimum 32 random characters |
| `COURTFINDER_GOOGLE_WEB_CLIENT_ID` | OAuth 2.0 Web Client ID | Used for Google Sign-in and account deletion |
| `COURTFINDER_FIREBASE_PROJECT_ID` | Firebase Project ID | e.g. `courtfinder-e36ea` |
| `COURTFINDER_FIREBASE_SERVICE_ACCOUNT_JSON` | Firebase Admin private service key JSON | Secret JSON string (never commit to git) |

---

## 3. Endpoints Provided

All routes match the CourtFinder Android app's Retrofit client contract:

- `GET  /` - Backend status landing page
- `GET  /api/health` - Health check (`{ "status": "ok" }`)
- `POST /api/auth/register` - User registration with PBKDF2-HMAC-SHA256 password hashing
- `POST /api/auth/login` - User login with email/phone & password
- `POST /api/auth/google` - Google & Firebase Sign-In token verification
- `GET  /api/auth/me` - Authenticated user profile (`Authorization: Bearer <token>`)
- `GET  /api/courts/search?query=...` - Search courts with sport category filtering
- `POST /api/bookings` - Authenticated court time slot booking
- `DELETE /api/account` - Authenticated account and booking deletion
- `GET  /delete-account` - Web portal for Google Play account deletion compliance

---

## 4. Connecting Your Android App

Once deployed, copy your Vercel public URL (e.g. `https://your-backend.vercel.app`) and set it in `/Users/yaswanth/courtFinder/local.properties`:

```properties
API_BASE_URL=https://your-backend.vercel.app
```

Rebuild the Android application:
```bash
./gradlew assembleDebug
```
The app will automatically point all Retrofit network requests to your live Vercel backend.
