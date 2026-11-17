# CourtFinder

CourtFinder is an Android sports court discovery app built with Kotlin, Jetpack Compose, Material 3, MVVM, coroutines, Room, and Retrofit. The repository also contains a Kotlin/Ktor backend for development and deployment.

## Current app behavior

- Search and filter sports court listings by sport, favorites, and rating
- View court details and map markers; request approximate location only when using the location control
- Cache court listings, favorite flags, and availability preferences in Room
- Register and sign in with email/password or configured Google sign-in
- Request a booking slot from the configured service
- Delete an account in the app or through the service's `/delete-account` page

The sample courts are fictional and seed development mode only. Production mode starts without demo courts. Availability preferences are stored on-device; push notifications are not implemented. Do not publish until the service contains real venue data and real availability, and the listed features have been verified with that data.

## Requirements

- Android Studio and Android SDK Platform 37
- JDK 23 or newer
- Google Maps API key and Google OAuth client IDs for those integrations

## Run the backend locally

From the repository root in PowerShell:

```powershell
$env:COURTFINDER_ENV = "development"
$env:COURTFINDER_FIREBASE_PROJECT_ID = "courtfinder-e36ea"
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\path\outside\repo\firebase-admin-service-account.json"
$firebaseConfig = Get-Content -Raw .\app\google-services.json | ConvertFrom-Json
$env:COURTFINDER_GOOGLE_WEB_CLIENT_ID = ($firebaseConfig.client[0].oauth_client | Where-Object { $_.client_type -eq 3 } | Select-Object -First 1).client_id
.\gradlew.bat -p backend run
```

The service listens on port 8080 and creates `backend/data/courtfinder.db` for local development. Development mode seeds fictional demo listings; other environments do not. Production requires `COURTFINDER_JWT_SECRET` (at least 32 characters) and a durable PostgreSQL `DATABASE_URL`; it refuses to start with SQLite. `COURTFINDER_PORT` overrides the server port (Vercel's `PORT` is also supported), and `COURTFINDER_DB_PATH` only applies to SQLite development.

API and web routes:

- `GET /api/health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/google` (verifies Firebase Google ID tokens using Firebase Admin; also accepts Google ID tokens from the account-deletion web flow)
- `GET /api/auth/me` (Bearer token)
- `GET /api/courts/search?query=`
- `POST /api/bookings` (Bearer token)
- `DELETE /api/account` (Bearer token; deletes the account and booking records)
- `GET /delete-account` (external account deletion web resource)

Google sign-in uses Android Credential Manager, Firebase Authentication, and backend-verified Firebase ID tokens. The backend also retains verification for raw Google ID tokens used by its account-deletion web page. Create an Android OAuth client restricted to package `com.yaswanth756.courtfinder` and each relevant signing certificate.

### Configure Google sign-in for local development

The **Continue with Google** button requires Google OAuth credentials, the Firebase Google provider, a configured service URL, and valid Firebase Admin credentials on the backend. The app passes the Web client ID from `google-services.json` (`default_web_client_id`) to Credential Manager; `GOOGLE_WEB_CLIENT_ID` in `local.properties` can override it. It must be the Web application client ID, not the Android client ID. See the [Android Credential Manager setup guide](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation) and [Firebase Android Google sign-in guide](https://firebase.google.com/docs/auth/android/google-signin).

For the debug app, register an Android OAuth client with package `com.yaswanth756.courtfinder` and this debug signing SHA-1:

```text
7F:22:21:64:D2:D4:51:37:CC:8E:8F:8C:1E:A0:5B:A7:21:41:4E:2F
```

The SHA-1 above is for this machine's debug keystore only. A Play release needs the SHA-1 for the Play App Signing certificate as well. Then place the Web client ID in the ignored root `local.properties` and set the identical value for the backend process:

```properties
GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
```

```powershell
$env:COURTFINDER_GOOGLE_WEB_CLIENT_ID = "your-web-client-id.apps.googleusercontent.com"
```

Firebase Admin token verification also needs the Firebase project ID and Application Default Credentials. Locally, keep a Firebase service-account key outside this repository and set:

```powershell
$env:COURTFINDER_FIREBASE_PROJECT_ID = "your-firebase-project-id"
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\private\firebase-admin-service-account.json"
```

On a cloud host, use its attached service account / Application Default Credentials or provide the service account JSON through the host's secret environment variable `COURTFINDER_FIREBASE_SERVICE_ACCOUNT_JSON`. The Admin service account must belong to the same Firebase project. Never put Admin credentials in the Android app or commit them. Without Admin credentials, other backend routes can start, but Firebase Google sign-in returns HTTP 503. The `google-services.json` file belongs at `app/google-services.json`; refresh it after adding signing fingerprints or changing Firebase provider settings.

For the Vercel container setup and production environment checklist, see [VERCEL_DEPLOYMENT.md](VERCEL_DEPLOYMENT.md).

For a local-backend debug session, use `API_BASE_URL=http://127.0.0.1:8081/` in `local.properties` and keep the backend running on port 8081. Android Studio's debug install now reapplies ADB port forwarding to online connected devices automatically. For an APK installed manually, run `adb reverse tcp:8081 tcp:8081` for the selected device. A physical phone needs USB ADB forwarding or the computer's Wi-Fi IPv4 address with both devices on the same network. The Google button is disabled until the OAuth client ID, Firebase app config, and API URL are available; debug's mock API cannot authenticate Google tokens.

## Run the Android app

Set local-only values in the ignored root `local.properties`:

```properties
API_BASE_URL=http://10.0.2.2:8080/
MAPS_API_KEY=your_maps_api_key
GOOGLE_WEB_CLIENT_ID=your-web-oauth-client-id.apps.googleusercontent.com
PRIVACY_POLICY_URL=https://your-public-domain/privacy-policy
```

`10.0.2.2` reaches the host computer from a standard Android emulator. For a physical device, use the host's LAN address. Debug builds allow HTTP for local development; release builds require HTTPS. Without `API_BASE_URL`, debug uses the mock API and release shows a service-unavailable state.

Build and verify locally:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

## Play Store status

The app targets API 37, above Google Play's current API 36 minimum for new apps. Account deletion is available in-app and through the backend's `/delete-account` page. The `bundleRelease` task is gated until the production API, OAuth, Maps key, privacy URL, upload signing key, and owner release-readiness flag are configured. This avoids generating a Play bundle with development or incomplete settings.

The app shows a Privacy policy link on the sign-in and home screens. The draft at [PRIVACY_POLICY_DRAFT.md](PRIVACY_POLICY_DRAFT.md) still contains owner-specific placeholders and is not ready to publish. See [PLAY_STORE_SUBMISSION.md](PLAY_STORE_SUBMISSION.md) for the launch checklist and [PRIVACY_DATA_INVENTORY.md](PRIVACY_DATA_INVENTORY.md) for current data handling.

## License

See [LICENSE](LICENSE).
