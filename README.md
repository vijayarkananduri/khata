# Khata

**Your money, a little clearer.** Khata is a native Android personal-finance tracker built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, and Hilt. It stores transaction and account data locally, works offline, and does not use ads, analytics, or telemetry.

## What works in this build

- Manual bank, cash, and wallet accounts with starting balances; assign every new transaction to an account.
- Quick Add with amount, income/expense, category, optional note, and a date picker for today or any earlier date.
- Offline transactions, deterministic merchant/keyword categorization, review queue, and learned merchant rules after correction.
- Home budget/comparison ring, top categories, recent activity, monthly calendar, year heatmap, analysis, search, and transaction details.
- Google Sans UI type and Caveat accent type, a custom wallet app icon, animated capsule navigation, optional Dew frosted ambient surfaces, haptics, and reduced-motion handling.
- User-requested Android notification permission and budget-crossing alerts. Alerts only work after the user grants permission and sets a budget.
- CSV sharing/export and five-language quote catalog.

Financial records and settings stay in local app storage. Android cloud backup is disabled. No bank password, PIN, OTP, or full account number is collected.

## Account Aggregator integration

Manual accounts work now. Live Indian Account Aggregator sync is **not enabled in this app**. The ReBIT flow requires an FIU platform with consent requests, callbacks, and secure financial-information fetches, while Sahamati documents FIU onboarding and regulatory eligibility. Khata therefore does not display a fake connected state or accept bank credentials. A real live integration needs an eligible FIU partner and provider onboarding/API configuration before implementation can be completed.

- [ReBIT FIU callback specification](https://api.rebit.org.in/spec/fiu)
- [ReBIT Account Aggregator API overview](https://api.rebit.org.in/spec/aa)
- [Sahamati FIU onboarding guide](https://sahamati.org.in/how-to-join-the-account-aggregator-network-to-share-and-access-financial-data/)

## Build locally

Requirements: JDK 17 and Android SDK platform 35/build tools. From the project root:

```sh
./gradlew testDebugUnitTest assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

Every push to `main` and manual `workflow_dispatch` runs `.github/workflows/android.yml`. The job runs unit tests, assembles the debug APK, and uploads a `khata-debug-apk` artifact. In GitHub, open **Actions → Build APK → run → Artifacts** and download it.

## Release and signing

The CI APK is debug-signed and suitable for installation/testing; it is not a Play Store production build. To make a Play-ready build, configure a user-owned signing key through GitHub Actions secrets (`KEYSTORE_B64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) and add a separately verified release-signing job. Never commit a keystore or passwords.

## Font licenses

Bundled Google Sans and Caveat font software is provided under SIL Open Font License 1.1. License copies are in `app/src/main/assets/licenses/`.
