# Live Tiles - Notification Integration Walkthrough

Implemented Task 17 to bring real-time notification integration to the launcher tiles, creating a dynamic "Live Tile" experience similar to Windows 10/11.

## Changes Made

### 1. Notification Listener Service
- **[WindowsNotificationListener.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/services/WindowsNotificationListener.kt)**: A new service that monitors system notifications. It groups active notifications by package name and extracts content summaries (sender and message text).
- **[AndroidManifest.xml](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/AndroidManifest.xml)**: Registered the `WindowsNotificationListener` service with the required `BIND_NOTIFICATION_LISTENER_SERVICE` permission.

### 2. Data Model & Management
- **[HomeTile.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/data/HomeTile.kt)**: Updated the data class to include `notificationCount` and `notificationSummary`.
- **[NotificationManager.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/data/NotificationManager.kt)**: A new singleton object that holds the current state of notifications across the app using a `StateFlow`.

### 3. Launcher Integration
- **[HomeViewModel.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeViewModel.kt)**: Updated the `tiles` flow to combine the persistent tile configuration from `SettingsRepository` with the real-time notification data from `NotificationManager`.

### 4. UI Enhancements
- **[HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)**:
    - Added a **Notification Badge** in the bottom-right corner of tiles that have active notifications.
    - Updated **WIDE** and **LARGE** tiles to display a notification summary (sender/title and message body) when available, matching the Live Tile aesthetic.
    - Adjusted layouts to ensure clear typography and spacing, following Fluent Design principles.
- **[SettingsScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/settings/SettingsScreen.kt)**: Added a new setting item under the "System" section to enable "Live Tiles (Notifications)", which directs the user to the system's Notification Access settings.

## Verification
- Project builds successfully.
- Code follows the requested architecture (NotificationListenerService -> NotificationManager -> HomeViewModel -> HomeScreen).
- UI components (Badges, Summaries) are conditionally rendered based on notification state and tile size.

## Screenshots/Preview (Simulated)
- **Medium Tile**: Shows an icon, label, and a badge with the count if notifications exist.
- **Wide/Large Tile**: Shows the icon in the corner, a bold label, and the notification summary text in the body.
