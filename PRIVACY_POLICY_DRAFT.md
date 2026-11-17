# Privacy policy draft for CourtFinder

**Draft only. Do not publish this file until all bracketed owner fields are completed and the text is reviewed for the actual production services and legal jurisdictions.**

Effective date: [set date]

## Who operates CourtFinder

CourtFinder is operated by [legal developer or organization name]. Privacy questions can be sent to [working privacy contact email or support URL].

## Information used by CourtFinder

When you create an account, CourtFinder receives your name, email address, optional phone number, and password. The service stores the account identifier and a salted password hash; it does not store the submitted password in plain text. When you sign in with Google, CourtFinder verifies the Google ID token and stores the provider subject, verified email, and display name needed to identify your account.

When you use search or booking features, the service processes court IDs, search terms, selected time slots, booking dates, and the account identifier associated with a booking. Favorites and availability-alert flags are cached on your device. This build does not send alert notifications.

If you choose the location feature and grant approximate-location access, the app uses the current location to sort results and request a reverse-geocoded address. Android's location/geocoding services and Google Maps may process information according to their own terms and privacy notices. The app does not continuously track location.

## How the information is used and shared

Account information is used for authentication and account management. Booking details are used to process booking requests and prevent duplicate slot reservations. CourtFinder sends requests to its configured service and uses Google identity and Maps services when those features are configured. [Name all production hosting, database, email, and other processors and describe any additional sharing before publication.]

## Storage and security

The backend stores account and booking records in its configured database. The app stores the session token encrypted with Android Keystore and excludes that preference file from device backup. Passwords are stored as salted PBKDF2 hashes. [Describe production transport encryption, access controls, backup protection, and incident contact process.]

## Retention and deletion

You can request account deletion in the app or at [public HTTPS deletion URL]. Account deletion removes the account and its booking records from the active CourtFinder database. [Set backup expiration/retention period and describe any legally required retained records.] Local cached court data is cleared during account deletion.

## Children's privacy

[Describe the intended age group and whether the service is directed to children.]

## Contact and changes

For privacy questions, contact [privacy contact]. This policy may be updated when CourtFinder's data practices change. The current version will be available at [public HTTPS policy URL].
