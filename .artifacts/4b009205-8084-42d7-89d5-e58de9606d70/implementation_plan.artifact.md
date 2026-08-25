# Implementation Plan - Task 19: Advanced Fluent Context Menus

Implement a high-fidelity, acrylic-styled context menu that appears on long-press of a home tile. This menu will feature notification details, interactive actions, and Fluent UI aesthetics.

## Proposed Changes

### 1. Data Layer Enhancements

#### [MODIFY] [NotificationData.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/data/NotificationData.kt)
- Add `sender`, `content`, and `postTime` fields to `NotificationData`.

#### [MODIFY] [WindowsNotificationListener.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/services/WindowsNotificationListener.kt)
- Update `updateNotifications()` to extract sender name, message content, and post time from `StatusBarNotification`.

#### [MODIFY] [HomeTile.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/data/HomeTile.kt)
- Add `notificationSender`, `notificationContent`, and `notificationTime` to the `HomeTile` data class.

#### [MODIFY] [HomeViewModel.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeViewModel.kt)
- Map the new `NotificationData` fields to `HomeTile` in the `tiles` flow.

### 2. UI Components

#### [NEW] [AdvancedFluentMenu.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/components/AdvancedFluentMenu.kt)
- Implement `AdvancedFluentMenu` composable:
    - **Acrylic Overlay**: Uses `FluentSurface` with high blur and custom transparency.
    - **App Header**: Displays app icon and name.
    - **Notification Section**: Shows sender, content, and relative time if a notification is active.
    - **Interactive Reply**: Adds a placeholder/text field for replies.
    - **Quick Actions**: "App Settings", "Resize Mode", and "Dismiss".

### 3. Integration

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Replace the legacy `TileContextMenu` with the new `AdvancedFluentMenu`.
- Implement a full-screen blurred backdrop when the menu is active to emphasize the Acrylic overlay.

## Verification Plan

### Automated Tests
- `./gradlew :app:assembleDebug` to ensure compilation.

### Manual Verification
- Long-press a tile to trigger the menu.
- Verify Acrylic blur effect and 28dp+ rounded corners.
- Verify notification details (Sender, Content, Time) are correctly displayed.
- Verify action buttons (Resize, App Info) function as expected.
- Verify dismissal by clicking the backdrop or the close button.
