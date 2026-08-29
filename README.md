# Nimo

Nimo is a Java-first voice assistant companion for Android and Chrome. It combines push-to-talk speech recognition, a warm female-voice speech response, practical local actions, sign-up scaffolding, and an optional Java backend for live AI answers.

## What is included

| Component | Technology | Purpose |
|---|---|---|
| Android app | Java, Android SDK, Gradle | Installable APK project with voice input, spoken replies, sign-up screen, local commands, and chat UI |
| Chrome extension | Manifest V3, HTML, CSS, plain JavaScript | Browser popup with voice input, spoken replies, local commands, sign-up screen, and API URL settings |
| Backend | Java 17, JDK HTTP server, Gradle | `/health`, `/api/auth`, and `/api/chat`; optional OpenAI-compatible live responses |

The interface deliberately avoids glassmorphism and AI watermarks. It uses tactile rounded cards, a paper-like warm base, and green, blue, orange, and white accents.

## Run the Java backend

Install JDK 17 and Gradle, then run:

```bash
cd backend
export OPENAI_API_KEY="your-key"
export OPENAI_BASE_URL="https://api.openai.com/v1"
export OPENAI_MODEL="gpt-4o-mini"
gradle run
```

Without `OPENAI_API_KEY`, Nimo still runs in offline mode and answers local built-in commands. The server listens on `http://localhost:8080`.

## Build the Android APK

Open the `android` folder in Android Studio. Android Studio will sync the Gradle project. Connect a device or start an emulator, then use **Build > Build APK(s)**. The generated APK is placed under `android/app/build/outputs/apk/`.

The emulator uses `http://10.0.2.2:8080` to reach a backend running on the development computer. For a physical phone, change the `API` constant in `MainActivity.java` to the computer's LAN address or your deployed HTTPS backend.

The app asks for microphone permission at first use. Android's available system voices vary by device; Nimo selects a voice whose installed name suggests a female voice and otherwise uses the default US English voice.

## Load the Chrome extension

Open Chrome at `chrome://extensions`, enable **Developer mode**, choose **Load unpacked**, and select the `extension` directory. Use the gear button inside the popup to change the backend API URL. The browser must allow microphone access for voice recognition.

## Supported practical commands

Nimo can say the current time, open Google or YouTube, set a one-minute demonstration timer, explain its capabilities, and send general questions to the configured live AI backend. The assistant does not pretend to perform actions that are unavailable to the device or browser.

## Security notes

The included backend is a compact starter server intended for local development and controlled deployment. Before production use, replace the in-memory account store with a durable database, use Argon2id or bcrypt with per-user salts, add HTTPS, rate limiting, real session tokens, refresh-token rotation, audit logging, and a restrictive CORS policy. Never place a production AI API key inside the APK or extension.

## License

Add the license of your choice before publishing. This repository contains no branding, watermark, or copyrighted Iron Man assets.
