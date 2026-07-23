# Finger Address App

An Android app that uses **fingerprint recognition** to identify people and display their home address and emergency contact — designed to help people with special needs (non-verbal, hearing impaired, etc.) be identified and reunited with their families.

---

## How It Works

| Screen | What it does |
|--------|-------------|
| **Enroll** | Fill in Name, Home Address, Father's Mobile → Scan fingerprint → Profile saved to Firebase |
| **Verify** | Scan fingerprint → Profile opens instantly showing name, address, and father's contact |

> The app works **per device**: the enrolled person carries the app on their own phone. When they are found lost, anyone can open the Verify screen and scan their finger to see who they are and how to contact family.

---

## Setup Instructions

### Step 1 — Create a Firebase Project

1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Click **Add project** → name it `FingerAddress`
3. Go to **Project Settings** → **Your apps** → click the Android icon
4. Register app with package name: `com.fingeraddress.app`
5. Download `google-services.json`
6. Place the file at: `app/google-services.json`

### Step 2 — Enable Firestore

1. In Firebase Console → **Firestore Database** → **Create database**
2. Start in **test mode** (you can add security rules later)
3. Choose a region close to you

### Step 3 — Enable Anonymous Auth (optional but recommended)

1. Firebase Console → **Authentication** → **Sign-in method**
2. Enable **Anonymous**

### Step 4 — Firestore Security Rules (for production)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /profiles/{deviceId} {
      allow read, write: if true; // tighten this for production
    }
  }
}
```

---

## Building the APK

### Locally (Android Studio)
1. Open the project in Android Studio
2. Place `google-services.json` in the `app/` folder
3. Click **Build > Build Bundle(s)/APK(s) > Build APK(s)**
4. APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### Via GitHub Actions (automatic)
1. Push `google-services.json` content as a GitHub secret named `GOOGLE_SERVICES_JSON`
2. Create a tag: `git tag v1.0.0 && git push origin v1.0.0`
3. GitHub Actions builds the APK and creates a **Release** automatically
4. Download the APK from the **Releases** tab

---

## Project Structure

```
app/
├── src/main/
│   ├── java/com/fingeraddress/app/
│   │   ├── MainActivity.kt         # Home screen (Enroll / Verify buttons)
│   │   ├── EnrollActivity.kt       # Form + fingerprint enrollment + Firebase save
│   │   ├── VerifyActivity.kt       # Fingerprint scan to open profile
│   │   ├── ProfileActivity.kt      # Show name, address, call/share buttons
│   │   └── model/UserProfile.kt    # Data model
│   └── res/
│       ├── layout/                 # 4 XML layouts
│       ├── drawable/               # Vector icons
│       └── values/                 # Colors, strings, themes
```

---

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: Android 6.0 (API 23)
- **Biometrics**: AndroidX BiometricPrompt (works on all Android 6+ devices)
- **Database**: Firebase Firestore
- **UI**: Material Design 3 components
- **CI/CD**: GitHub Actions → auto-releases APK on tag push

---

## Requirements

- Android phone with fingerprint sensor
- Android 6.0 or above
- Internet connection (for Firebase sync)
- Firebase project with Firestore enabled

---

## Privacy Note

Fingerprint data is **never stored** — Android's BiometricPrompt API only confirms that the fingerprint matches one registered on the device. Only the profile data (name, address, phone number) is stored in Firebase Firestore, linked to the device's unique Android ID.
