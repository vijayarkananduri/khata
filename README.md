<div align="center">

# 📒 Khata

**Your money. Your phone. Nobody else.**

A private, offline money tracker for Android. No bank linking, no sign-up, no cloud.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Offline](https://img.shields.io/badge/works-100%25%20offline-blue)
![Privacy](https://img.shields.io/badge/bank%20linking-none-red)

[Download latest release](https://github.com/vijayarkananduri/khata/releases/latest)

</div>

---

## What is Khata?

*Khata* means a ledger, the book where you write down what came in and what went out.

This app is that book, on your phone. You note down your spending yourself, and everything stays on your device. There is no bank connection, no account, and no server that sees your data.

## Who is it for?

**Khata is for you if:**
- You value your privacy and don't want an app reading your bank messages or accounts
- You like to log your own spending and see where your money goes
- You want something calm and simple, not a finance dashboard full of AI predictions

**Khata is not for you if:**
- You want transactions to be tracked automatically from your bank
- You want AI-driven insights, forecasts or financial advice

Khata is about tracking, not about being clever with your money.

## Features

### 📒 Private tracking
- Log expenses, income and transfers in a few taps
- No bank linking, fully offline
- Your history stays on your device, and you can export it as CSV

### 🟩 Spending heatmap
See your whole year at a glance in a GitHub-style heatmap. Darker days mean heavier spending, so your habits show up without any charts to read.

### 🔔 Nudges (smart restock reminders)

Khata learns your buying pattern and reminds you before you run out.

**How it works**
1. You log something you buy regularly, like milk, groceries or petrol.
2. Once Khata has seen the same item more than twice, it works out how often you buy it.
3. When the next purchase is due, it sends a gentle reminder.

> You buy 1 litre of milk, then again after 2 days, then again after 2 days. Khata sees the pattern: a litre lasts you about 2 days. On day 2 it reminds you: *"You may be running out of milk."*

It works the same way for groceries, petrol and other essentials. The pattern is calculated on your phone from your own entries, and nothing is sent anywhere.

Don't want reminders? Switch nudges on or off any time in **Settings**.

### 🧩 Home-screen widgets
Add widgets to your home screen to see your spending at a glance without opening the app.

### 🎬 A bit of personality
Money tracking doesn't have to be boring. Khata shows short quotes inspired by movies and memes, so checking your spending feels a little lighter.

### 🌐 Your language
Hindi, Telugu, Tamil, Malayalam and Kannada, in the original script or in English letters.

### 🎨 Themes
Light, Dark, System, or colours taken from your Android wallpaper.

## Install

1. Open the [latest release](https://github.com/vijayarkananduri/khata/releases/latest)
2. Download the APK and install it on your phone
3. Allow installs from your browser or file manager if asked

## Build from source

Requires JDK 17 and Android SDK 35.

```bash
git clone https://github.com/vijayarkananduri/khata.git
cd khata
./gradlew testDebugUnitTest assembleDebug
```

## Built with

Kotlin · Jetpack Compose · Room

## Privacy promise

Khata never asks for your bank details, never needs an account, and works without internet. Your data leaves your phone only if you choose to export it.

