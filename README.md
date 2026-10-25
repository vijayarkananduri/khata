<div align="center">

# Khata

**Your money. Your phone. Nobody else.**

A private, offline money tracker for Android.
No bank linking. No sign-up. No cloud. No judgment (mostly).

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Offline](https://img.shields.io/badge/works-100%25%20offline-blue)
![Bank linking](https://img.shields.io/badge/bank%20linking-none-red)
![License](https://img.shields.io/badge/license-MIT-green)

[Download the latest APK](https://github.com/vijayarkananduri/khata/releases/latest)

</div>

---

<!-- Add 3 or 4 screenshots here. Example:
<p align="center">
  <img width="720" height="1570" alt="WhatsApp Image 2026-10-25 at 10 17 08 AM (1)" src="https://github.com/user-attachments/assets/5f5d6c79-1df7-472a-8f41-a915684aed2b" />
  <img width="720" height="1572" alt="WhatsApp Image 2026-10-25 at 10 17 08 AM" src="https://github.com/user-attachments/assets/a6ce9fe4-329c-4d61-aa15-127eba841312" />

<img src="docs/screenshots/home.png" width="220" />
  <img src="docs/screenshots/heatmap.png" width="220" />
  <img src="docs/screenshots/widgets.png" width="220" />
</p>
-->

## The pitch

*Khata* is the old word for the ledger, the book where the shopkeeper wrote down what came in and what went out.

This is that book, on your phone. You write the entries. The app keeps them. Nobody else gets a copy, because there is no "else": no server, no account, no analytics team studying your chai habit.

## Khata is for you if

- You think your bank statement is none of an app's business
- You are happy to type in your own spending
- You would rather see where your money went than be told how to invest it

## Khata is not for you if

- You want expenses to appear automatically from your bank
- You want AI forecasts, scores or "smart insights"

It tracks. That's the whole job. It does the job quietly.

## What it does

### Tracks, privately
Log expenses, income, transfers and old transactions in a few taps. Everything stays on your device, and you can export it all as CSV whenever you like.

### Shows your year like a commit graph
A GitHub-style heatmap of your spending. Dark squares are the days your wallet had opinions. Your habits show up without you reading a single chart.

### Nudges you before you run out
Khata watches how often you buy the same thing and works out how long it lasts.

1. You log something you buy regularly: milk, groceries, petrol.
2. After it has seen the same item more than twice, Khata works out how often you buy it.
3. When the next purchase is due, it taps you on the shoulder.

> You buy a litre of milk. Two days later, you buy a litre of milk. Two days after that, you buy a litre of milk. Khata gets it: a litre lasts you two days. On day two, it says, *"You may be running out of milk."*

Same trick for groceries, petrol and other essentials. The maths happens on your phone, using only your own entries. Don't want the reminders? Turn nudges off in Settings.

### Lives on your home screen
Widgets for daily, weekly, monthly or all-time totals, and for monthly budget progress, so you can check your spending without opening the app and without pretending you didn't.

### Has a personality
Short quotes inspired by movies and memes show up in the app, because money tracking does not need to feel like a tax audit.

### Speaks your language
Hindi, Telugu, Tamil, Malayalam and Kannada, in the original script or in English letters.

### Matches your phone
Light, Dark, System, or colours pulled from your wallpaper.

## Things Khata will never do

- Ask for your bank login
- Ask you to create an account
- Need internet to work
- Send your data anywhere unless you export it yourself

## Install

1. Open the [latest release](https://github.com/vijayarkananduri/khata/releases/latest)
2. Download the APK and open it on your phone
3. If Android asks, allow installs from your browser or file manager

## Build it yourself

You need JDK 17 and Android SDK 35.

```bash
git clone https://github.com/vijayarkananduri/khata.git
cd khata
./gradlew testDebugUnitTest assembleDebug
```

## Built with

Kotlin, Jetpack Compose and Room.

## Contributing

Found a bug, or have an idea that fits the "private, simple, offline" rule? Open an issue. Ideas that need a server will be politely declined.

## License

Khata is open source under the [MIT License](LICENSE).
Bundled fonts have their own licenses in `app/src/main/assets/licenses/`.
