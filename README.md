# Jarvis Assistant (Android)

A native Java Android starter app with a dark chat interface, speech-to-text input through Android's speech recognition activity, and spoken responses using Android Text-to-Speech.

## Features

- Voice input and typed chat
- Spoken assistant responses
- Local time and date answers
- Open a website or search the web
- No API key or network permission is needed by the app itself; web searches are handed off to a browser

This is a simple starter assistant, not a full conversational AI. Connecting an AI provider would require choosing a service and protecting its API key on a backend rather than embedding a secret in the Android app.

## Open and run

Open this repository in Android Studio, allow Gradle to sync, then run the `app` configuration on an Android device or emulator. The Android Gradle Plugin requires JDK 17. Voice recognition availability depends on the device and its installed speech services.
