# Khata

**Everyday money, in balance.**

Khata is a polished, offline-first Android companion for tracking spending, income, budgets and everyday money habits. It keeps your records on your device and turns them into a clear, calm picture of where your money goes.

## A clearer money picture

- Log expenses, income, transfers and past transactions in a few taps.
- Explore a refreshed analysis dashboard with a category donut, readable breakdowns, a calendar and a year heatmap.
- Add separate home-screen widgets for daily, weekly, monthly or all-time totals, monthly budget progress, or a quote.
- Set a monthly budget and opt in to budget alerts and local restock nudges for groceries, fuel and essentials.
- Use Hindi, Telugu, Tamil, Malayalam or Kannada; choose original script or English-letter transliteration.
- Choose Light, Dark, System or Android wallpaper-derived color themes. Motion and haptic feedback respect app/device settings.
- Keep your financial history local and export it as CSV.

Quote cards show a direct line only. Short original-language excerpts are attributed in the bundled catalog to published dialogue sources; translations and commentary are not rendered as quotes.

## Build and test

Use JDK 17 and Android SDK 35:

```bash
./gradlew testDebugUnitTest assembleDebug
```

GitHub Actions runs unit tests and builds an APK on pushes to `main`. Tagged releases run the same tests and publish an APK. When owner signing secrets are configured, the APK is signed with that owner key; otherwise, the release is marked as a debug-signed test build.

[Latest Khata release](https://github.com/vijayarkananduri/khata/releases/latest)

## Licensing

This repository currently has no open-source license selected. Do not assume permission to redistribute or modify it beyond the repository's existing terms. Bundled font license files are in `app/src/main/assets/licenses/`.
