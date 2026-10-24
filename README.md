# Khata

**Every rupee, in its place.** Khata is a native Android personal-finance tracker built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, and Hilt. It is dark-first, works offline, and does not use AI/ML, ads, analytics, or telemetry.

## Current build

The app includes a five-step onboarding flow, manual expense/income entry, local transaction persistence, deterministic merchant/keyword categorization, an uncategorized review queue, learned merchant rules after correction, Home spending ring and category summary, a monthly heatmap calendar, analysis/search, settings, CSV sharing/export, and the five-language quote catalog from the specification.

Bank Account Aggregator synchronization and CSV import are not connected in this build. Bank linking remains optional; manual tracking is available without it. Dew currently changes card surfaces without a platform blur effect. The Android debug APK is not release-signed.

## Build locally

Requirements: JDK 17 and Android SDK platform 35/build tools. From the project root:

```sh
./gradlew testDebugUnitTest assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

Every push to `main` and manual `workflow_dispatch` runs `.github/workflows/android.yml`. The run uploads a `khata-debug-apk` artifact. In GitHub, open **Actions → Build APK → run → Artifacts** and download it. A downloaded artifact is a ZIP containing `app-debug.apk`.

## Privacy and signing

Transaction records, merchant rules, and app preferences are stored locally on the device. No remote data service is configured. Debug builds use Android's debug signing. For a release build, add signing secrets (`KEYSTORE_B64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) to GitHub Actions secrets and configure a separate release-signing job; do not commit a keystore or passwords.
