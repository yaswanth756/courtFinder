# CourtFinder technical data inventory

This is an implementation inventory for preparing the public privacy policy. It is not a legal privacy notice. The app owner must confirm retention periods, operating jurisdictions, processors, contact details, account deletion procedures, and the final policy URL before publishing.

## Data handled by the current code

| Data | Where it goes or is stored | Purpose |
|---|---|---|
| Email address, full name, optional phone number | Sent to the configured CourtFinder API during registration; stored in the backend SQLite `users` table | Create and identify an account |
| Password | Sent to the API over the configured connection; backend stores a per-account random salt and PBKDF2-HMAC-SHA256 hash, not the submitted password | Account authentication |
| JWT session token | Stored locally as AES-GCM ciphertext in app preferences; encryption key is held by Android Keystore; preference file is excluded from cloud backup and device transfer | Keep the signed-in session across app launches |
| Court records, availability fields, favorites, alert flags | Court API and local Room database | Search display and offline cache |
| Booking court ID, time slot, date, and account ID | Sent to and stored by the backend | Create a booking and prevent duplicate slot reservations |
| Google account subject, verified email, and display name (when Google sign-in is configured) | Verified by the backend and linked to a CourtFinder account | Sign in without a CourtFinder password |
| Approximate device location | Requested only after the user taps the location action and grants permission; used to sort visible courts and request a reverse-geocoded address | Show nearby courts |
| Coordinates for reverse geocoding and map tiles | Passed to Android's geocoding provider and Google Maps SDK when those features are used | Resolve an address and render map content |

## Current limitations and owner decisions

- The development court list contains fictional sample records. It must be replaced before bookings are represented as real reservations.
- Password reset email, notification delivery, analytics, and crash-reporting integrations are not present.
- Account deletion is available in the app and through the backend's `/delete-account` web page. It deletes the account and its bookings from the active database; the operator must define backup expiration and verify provider-side deletion behavior before launch.
- The backend stores account details and booking data in SQLite. The operator must choose a hosting region, database protection and backup policy, retention period, and account deletion process.
- The app asks for approximate location only on demand. Confirm whether the selected Google and Android services' disclosures cover the final SDK configuration.
- Google OAuth and Maps credentials are not included in this repository. Google OAuth is wired, but must be configured with matching Android/Web clients and a verified consent screen.
- Google sign-in does not automatically link an existing password account with the same email; account linking needs a verified authenticated flow.
- Add the accountable entity name, privacy contact email, policy URL, retention terms, and jurisdiction-specific disclosures before app distribution.
