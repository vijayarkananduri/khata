# Khata

**Money tracking, with a little more character.**

Khata is an offline-first Android finance companion with a polished liquid-glass interface, witty money-movie quotes, and a home-screen glance at today's spending. Your records stay on your device. No ads, analytics, or telemetry.

## Highlights

- **See today at a glance.** Pin the Khata home-screen widget to keep today's recorded expenses close. It refreshes after a new entry and periodically in the background.
- **Get a useful nudge.** Optional local restock reminders infer a rough rhythm from at least two repeated grocery, fuel, health or pet-essential purchases. Turn them off in Settings at any time.
- **Enjoy a little money humor.** Film-inspired and meme-style quotes shuffle across splash and Home. The app avoids recent repeats for a week when enough alternatives exist.
- **Keep the essentials simple.** Add income and expenses, choose from your existing saved accounts, review categories, compare spending and export CSV. Bank/cash account creation is no longer offered; existing saved accounts and records remain intact.
- **Make the glass your default.** A permanent, high-transparency liquid-glass treatment, warm light or deep dark palette, haptics and reduced-motion preferences.

## Privacy

Transaction data and quote history are kept locally. Android cloud backup is disabled. There is no analytics, advertising, or telemetry SDK. Restock reminders require Android notification permission.

## Build

Requires JDK 17 and Android SDK platform 35/build tools. Run `./gradlew testDebugUnitTest assembleDebug`. GitHub Actions runs these checks for pushes to `main`; the debug APK is published as a run artifact.

GitHub Actions signing can use a user-owned keystore supplied through repository secrets `KEYSTORE_B64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`. Do not commit keystores or passwords. Without those secrets, CI's debug APK is for testing only and is not Play Store production-signed.

## Account linking

Live Indian Account Aggregator connections are not implemented. A real integration needs an eligible FIU partner and its consent/provider services; Khata does not simulate linked-bank status or collect bank credentials.

## License

An application license has not yet been selected; the repository remains private pending the owner’s license and publication choice. Bundled font license copies are in `app/src/main/assets/licenses/`.
