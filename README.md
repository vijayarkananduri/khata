# Khata

**A calmer way to keep up with everyday money.**

Khata is an offline-first Android finance companion for recording expenses and income, reviewing spending, and keeping useful totals close. It uses clear solid surfaces, warm colors, subtle depth, and no ads or analytics.

## What it does

- Record expenses and income; review categories, budgets, and recent transactions.
- Switch Home between daily, rolling 7-day, and monthly views.
- Pin a spending widget with daily, 7-day, and monthly totals.
- Use the light palette, dark palette, system theme, or Android wallpaper-derived colors where supported.
- Get local opt-in restock nudges. Your transaction records stay on device.
- Browse short, directly sourced movie lines in the selected original-language catalog. No translated commentary appears with the quote.

## Privacy

Transaction and quote history are stored locally. Android cloud backup is disabled. The app contains no analytics or advertising SDK. Reminder notifications require Android permission.

## Build

Requires JDK 17 and Android SDK platform 35. Run `./gradlew testDebugUnitTest assembleDebug`. GitHub Actions runs tests and publishes a debug APK artifact on pushes to `main`.

Owner-key release signing is configured through GitHub Actions secrets: `KEYSTORE_B64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`. Never commit signing keys/passwords. Without an owner keystore, APKs are debug-signed for testing and are not Play Store production-signed.

## Account linking

Live Indian Account Aggregator connections are not implemented. Khata does not collect bank credentials or simulate linked status.

## License

The repository is currently private and has no application license selected. Choose a license before publishing it as open source. Third-party font license copies are in `app/src/main/assets/licenses/`.
