# Khata

**Everyday money, in balance.**

Khata is a calm, offline-first companion for tracking daily spending, income, budgets, and money habits.

## Features

- Quickly record expenses, income, transfers, and past transactions.
- See spending at a glance with category insights, a calendar, and a year heatmap.
- Add resizable home-screen widgets for daily, weekly, monthly, or all-time totals.
- Set a monthly budget and opt in to budget alerts and gentle restock reminders.
- Review uncategorized entries; Khata learns your corrections on-device.
- Use Hindi, Telugu, Tamil, Malayalam, or Kannada, with optional English-letter transliteration.
- Keep records locally, export a CSV, and use the warm paper-and-Ember Dew design.

## Build

Use JDK 17 and Android SDK 35:

```bash
./gradlew testDebugUnitTest assembleDebug
```

GitHub Actions runs tests and builds an APK on pushes to `main`. Tagged releases are published from the Android release workflow. A release is owner-key signed when signing secrets are configured; otherwise it is marked as a debug-signed test prerelease.

[Latest Khata release](https://github.com/vijayarkananduri/khata/releases/latest)

The private repository has no open-source license selected. Bundled font license files are in `app/src/main/assets/licenses/`.
