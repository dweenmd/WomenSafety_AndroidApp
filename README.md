# 🛡️ WomenSafety Android App

An advanced, premium emergency alert application built for Android. It empowers women to send instant SOS notifications—with real-time location—to their emergency contacts during distress situations, featuring a sleek, production-ready interface.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/language-Java-007396?logo=java&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)
![Status](https://img.shields.io/badge/status-Active-success)

---

## 📖 Overview

WomenSafety is a modern, reliable, and easy-to-use safety companion app designed for quick action during emergencies. With a massive recent UI/UX overhaul, it now features a premium dark/light mode, smart contact management, advanced authentication, and immediate access to SOS tools—ensuring no time is wasted when it matters most.

---

## ✨ Key Features & Recent Updates

### 🎨 Premium UI/UX Redesign (v3.0)
- **Modern Themes**: Beautifully crafted Dark and Light modes using a professional color palette (Purple/Charcoal/Red).
- **Smooth Navigation**: Intuitive Bottom Navigation Bar (Home, Safety, Contacts, Profile).
- **Categorized Profile**: A sleek, iOS-style settings page organized into Account, Safety, and App sections.

### 🔒 Advanced Authentication
- **Secure Login & Registration**: Powered by Firebase Authentication.
- **Phone OTP Verification**: Fast and secure phone number login alongside traditional Email/Password methods.
- **Seamless Onboarding**: Clean, animated splash screen and onboarding flow.

### 👥 Smart Emergency Contacts
- **Phonebook Integration**: Add contacts manually or instantly fetch them from your device's native phonebook.
- **Primary & Secondary Prioritization**: Important contacts are highlighted at the top with a dedicated ⭐ Primary badge.
- **Interactive Forms**: Smooth Bottom Sheet dialogs for adding and editing contacts without leaving the screen.
- **Quick Actions**: One-tap Call and Message buttons directly on the contact cards.

### 🚨 Core SOS & Safety Features
- **Hold-to-SOS**: A clear, prominent panic button that triggers emergency mode.
- **Real-Time Location Sharing**: SMS alerts automatically include your current GPS live-link.
- **Shake to Alert**: Shake the phone to automatically trigger an SOS message with your location.
- **Fake Call**: Schedule a simulated incoming call to gracefully exit uncomfortable situations.
- **Emergency Audio Record**: Quickly start a background audio recording.
- **One-Touch Emergency Calling**: Automatically dials Bangladesh's national emergency services (109 / 999).

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| Architecture | MVVM (ViewModel, LiveData) |
| UI | XML, Material Components 3 |
| Backend | Firebase (Auth, Firestore) |
| Local Storage | EncryptedSharedPreferences |
| Platform | Android SDK |
| Build Tool | Gradle |

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- [Android Studio](https://developer.android.com/studio) (latest version recommended)
- Java Development Kit (JDK 8 or higher)
- Android SDK

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/dweenmd/WomenSafety_AndroidApp.git
   ```
2. **Open the project** in Android Studio.
3. **Configure Firebase**:
   - Ensure your `google-services.json` is placed inside the `app/` directory.
4. **Sync Gradle** and let it resolve all dependencies.
5. **Run the app** ▶️

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch
   ```bash
   git checkout -b feature-branch
   ```
3. Commit your changes
   ```bash
   git commit -m "Add new feature"
   ```
4. Push to your branch
   ```bash
   git push origin feature-branch
   ```
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

<p align="center">Built with ❤️ to help keep people safe.</p>
