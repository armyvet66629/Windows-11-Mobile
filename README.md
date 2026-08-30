# Windows 11 Mobile Launcher for Android

A modern, highly customizable Android launcher that brings the **Fluent Design System** of Windows 11 to your mobile device. Built with Jetpack Compose, this launcher emphasizes glassmorphism, responsive "Live Tiles," and a professional, productive workflow.

## 🚀 Key Features

### 💻 Start Screen & Live Tiles
*   **Adaptive Grid**: Organizes apps into a flexible grid that supports Small, Medium, Wide, and Large tile sizes.
*   **Intelligent Flipping**: Tiles come alive with real-time information:
    *   **Clock & Weather**: Automatically flips to reveal a **5-day forecast** with high/low temperatures.
    *   **Music**: Full playback controls (Play/Pause, Skip) with a dynamic **album art background**.
    *   **Google Search**: Cycles through **top news stories** directly on the tile.
    *   **Universal Notifications**: Any app with a notification will automatically flip to show a brief summary of the alert.
*   **Precision Organization**: High-precision drag-and-drop mechanics with "locked" scrolling and tight hitboxes to prevent accidental movements.

### 👥 People Hub
*   **Social Dashboard**: A dedicated space for your favorite contacts and recent interactions.
*   **Real-time Activity**: Live tracking of **Calls, SMS, and MMS** with instant dashboard updates.
*   **Deep Linking**: Tap any recent message to jump directly into the specific conversation thread in your messaging app.

### 📝 Notes & Productivity
*   **Quick Notes**: A built-in scratchpad for capturing ideas and thoughts on the fly.
*   **Dashboard Widgets**: Integrated board for system widgets and a rich **RSS News Feed** featuring deep image discovery (extracting visuals from OpenGraph and article metadata).

### 🔍 App Drawer
*   **Fast Navigation**: Alphabetically grouped app list with a **Jump to Letter** overlay for instant scrolling through large app collections.
*   **Fluent Shortcuts**: Long-press any app to access Windows-style context menus with system-specific actions (e.g., "Take Photo" for Camera).

### 🎨 Customization & System
*   **Personalization**: Full control over accent colors, dark/light mode, and tile opacity.
*   **Status Bar Mastery**: Manually choose between Light or Dark status bar icons, or let the launcher handle it automatically based on your wallpaper.
*   **Page Manager**: Easily rearrange the order of your screens or "Hide" pages you don't use for a minimal setup.
*   **Haptic Feedback**: A premium, "textured" tactile experience during scrolling and page swiping.

## 🛠 Tech Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose
*   **Data Persistence**: DataStore & Room
*   **Networking**: Retrofit, OkHttp (RSS Parsing & News Discovery)
*   **Media**: Android MediaSession API (for Live Music Tiles)

## 📦 Installation
1.  Download the latest versioned APK from the repository (e.g., `Windows11Mobile-v1.0.1-debug.apk`).
2.  Install on any device running **Android 7.0 (API 24)** or higher.
3.  Set as the **Default Launcher** in Settings for the full experience.

---
*Developed with ❤️ and Fluent Design principles.*
