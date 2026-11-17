# Play Store submission checklist
**Status:** technical release preparation is underway; this is not ready for production submission until the owner-specific items below are complete. Google Play reviews whether the listing matches real app behavior.

## Technical package

- Package: `com.yaswanth756.courtfinder`
- Target API: 37 (Google Play currently requires API 36 or higher for new apps)
- Build artifact: Android App Bundle (`.aab`), signed with a private upload key
- Backend: Kotlin/Ktor service; set a production HTTPS `API_BASE_URL`
- Account deletion: in-app confirmation and service page at `https://<production-domain>/delete-account`
- Privacy policy: publish a completed policy on an accessible HTTPS URL and link it in the app and Play Console

## Store listing draft

Use only after the production API returns real venue data and every described feature has been exercised against it.

- App name: CourtFinder
- Short description: Find sports courts, compare details, and request available booking times.
- Full description: CourtFinder helps players discover sports courts by sport and location. Search court listings, review venue details and available time slots, save favorites, view map locations, and request bookings through the configured service. Approximate location is requested only when you choose the location feature.
- Category: Sports

Do not claim AI recommendations, live availability, completed reservations, or real venues unless the production service genuinely provides those features. Screenshots must show the production app and real content.

## Assets to upload

- High-resolution Play listing app icon: 512 × 512 PNG
- Feature graphic: 1024 × 500 PNG or JPEG
- At least two real screenshots across supported device types; current Play preview-asset rules require screenshots between 320 and 3840 pixels per dimension
- Optional preview video

## Play Console declarations

- Complete App access instructions with a working reviewer account if sign-in is required.
- Complete the Data safety form from a final audit of the app and every SDK, including Maps, Credential Manager, location/geocoding, and backend hosting.
- Provide the public privacy policy URL and the external account deletion URL above.
- Complete target audience, content rating, ads, app category, and contact details.
- If the developer account is a personal account created after 13 November 2023, plan for a closed test with at least 12 testers opted in continuously for 14 days, then apply for production access.

## Owner-provided items still required

- Play Console account and developer identity/contact details
- Production hosting, domain, HTTPS, database operations, secret management, and real court/availability source
- Google OAuth client IDs and verified consent screen; Maps key restricted to package and signing certificate
- Private upload keystore and passwords, stored outside source control; set `UPLOAD_STORE_FILE`, `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_ALIAS`, and `UPLOAD_KEY_PASSWORD` in ignored `local.properties`
- Privacy policy owner name, contact channel, retention periods, and final public URL
- Store icon/screenshots/feature graphic and reviewer credentials
- Closed-test participants and feedback process, if the account is subject to that requirement

After the production courts, privacy text, and app behavior are verified, set `PLAY_RELEASE_READY=true` in ignored `local.properties`. The `bundleRelease` task checks for this flag plus HTTPS service/policy URLs, OAuth and Maps keys, and complete signing values. Do not commit credentials or keystore files. Configure server secrets in deployment settings and signing/OAuth/map/privacy values in the ignored `local.properties` file.
