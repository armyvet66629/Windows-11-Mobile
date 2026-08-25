# Implementation Plan - Task 12: UI Refinements and Dock Management

This plan covers adding "Add to Home Screen" to the App Drawer, improving Home Screen transparency for wallpaper visibility, and adding Dock (Taskbar) management to Settings.

## Proposed Changes

### Home Screen & Shell
#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Remove opaque background from the root `Box`.
- Adjust internal gradient to be more subtle or transparent.

#### [MODIFY] [HomeViewModel.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeViewModel.kt)
- Add `addTile(packageName: String, label: String)` function.

### App Drawer
#### [MODIFY] [AppDrawerScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/apps/AppDrawerScreen.kt)
- Update `AppDrawerScreen` to accept `onAddToHomeScreen: (AppInfo) -> Unit`.
- Update `AppItem` to include "Add to Home Screen" in the context menu.

#### [MODIFY] [MainShell.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/shell/MainShell.kt)
- Pass `onAddToHomeScreen` from `MainShell` to `AppDrawerScreen`, calling `homeViewModel.addTile`.

### Settings & Dock Management
#### [MODIFY] [SettingsViewModel.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/settings/SettingsViewModel.kt)
- Expose `pinnedApps` from `SettingsRepository`.
- Add `pinApp(packageName: String)` and `unpinApp(packageName: String)`.
- Add `installedApps` StateFlow (retrieved from `AppRepository`).

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/settings/SettingsScreen.kt)
- Add "Manage Dock" section.
- List pinned apps with an "Unpin" button.
- Add "Add App to Dock" button that opens a dialog with the list of all installed apps.

## Verification Plan

### Automated Tests
- Build the project using `./gradlew assembleDebug`.

### Manual Verification
- Long-press an app in the App Drawer and select "Add to Home Screen". Verify a 2x2 tile appears on the Home Screen.
- Check if the wallpaper is visible behind the tiles on the Home Screen.
- Go to Settings -> Manage Dock. Try pinning and unpinning apps. Verify changes reflect in the Taskbar.
