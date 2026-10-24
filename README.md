# Khata

**A quieter, more intentional way to stay close to your money.**

Khata is a premium-feeling, offline-first Android money companion: clear daily spending, thoughtful quotes, and gentle reminders for the essentials you tend to restock. No ads. No analytics. No bank passwords. Your records stay on your device.

## A little more clarity, every day

- **Know where you stand.** A compact spending ring, budget comparison, category breakdown, and recent activity make the month easier to read.
- **Capture the moment.** Add income or expenses to bank, cash, or wallet accounts, with date, category, and notes.
- **Let patterns help.** Khata learns from repeat purchases in groceries, fuel, health and pet essentials. When a purchase rhythm is established, an optional local reminder can suggest checking whether you are running low.
- **Make it feel yours.** Choose a warm light or deep dark palette, fully translucent Dew glass, reduced motion, haptics, and one of five supported languages. Context-aware and splash quotes keep the experience human.
- **Keep control.** Export a CSV, turn nudges on or off, and keep financial data local. Android cloud backup is disabled.

## A note about smart nudges

Nudges infer a rough restock interval from repeat expense entries for the same merchant and supported category. They need at least two dated purchases to learn a pattern, run locally once daily, and require Android notification permission for background alerts. Estimates are suggestions—not inventory tracking—and can be turned off in Settings.

## What Khata does not pretend to do

Khata currently supports manually entered accounts; live Indian Account Aggregator connections are not included. A secure integration needs an eligible FIU partner and its provider onboarding, consent and callback services. Khata will not imitate a connected-bank state or collect bank credentials.

## Build and try

Requirements: JDK 17 and Android SDK platform 35/build tools. Run `./gradlew testDebugUnitTest assembleDebug`. The installable **debug APK** is `app/build/outputs/apk/debug/app-debug.apk`; it is for testing, not a Play Store production signature.

Every push to `main` runs unit tests and assembles a debug APK in GitHub Actions. A user-owned production signing key is not configured, so releases are explicitly debug-signed.

## Privacy and licenses

Transaction and preference data are stored locally; there is no analytics, advertising, or telemetry SDK. Google Sans and Caveat font license copies are in `app/src/main/assets/licenses/`.
