# Task 8: Personalization Hub and Settings Persistence

Implemented user settings persistence using Jetpack DataStore and created a Fluent Design-inspired Settings screen.

## Proposed Changes

### Data Layer
#### [NEW] [SettingsRepository.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/data/SettingsRepository.kt)
- Uses `DataStore<Preferences>` to store `isDarkMode` (Boolean) and `wallpaperUri` (String).
- Provides Flows for both settings.

### UI Layer - Settings
#### [NEW] [SettingsViewModel.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/settings/SettingsViewModel.kt)
- Exposes settings as `StateFlow`.
- Provides methods to update settings.
#### [NEW] [SettingsScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/settings/SettingsScreen.kt)
- Implements a modern settings interface using `FluentSurface`.
- Includes a toggle for Dark/Light mode.
- Includes a wallpaper picker using `PickVisualMedia`.

### Integration
#### [MODIFY] [MainActivity.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/MainActivity.kt)
- Instantiates `SettingsRepository`.
- Observes `isDarkMode` and updates `Windows11MobileTheme`.
#### [MODIFY] [MainShell.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/shell/MainShell.kt)
- Observes `wallpaperUri` and renders the image as the launcher background.
- Handles navigation to `Dest.Settings`.
#### [MODIFY] [AppDrawerScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/apps/AppDrawerScreen.kt)
- Adds a "Launcher Settings" entry at the top of the app list.

## Verification Plan
### Automated Tests
- Build success: `./gradlew :app:assembleDebug`

### Manual Verification
1. Open App Drawer.
2. Click "Launcher Settings".
3. Toggle Dark Mode and observe the UI theme change immediately.
4. Click "Wallpaper", select an image, and observe it appearing as the background of the launcher.
5. Restart the app and verify settings are persisted.
