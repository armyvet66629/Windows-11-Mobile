# Windows 11 Mobile Launcher Fixes

This plan addresses the layout bug with the Taskbar, replaces placeholder icons on the Home screen with real app icons, and ensures UI legibility.

## Proposed Changes

### [UI Components]

#### [MODIFY] [FluentSurface.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/components/FluentSurface.kt)
- Remove `Modifier.fillMaxSize()` from the content layer `Box`. This allows the surface to correctly wrap its content when `wrapContentSize()` is used in the parent modifier, preventing the Taskbar from expanding to cover the entire screen.

### [Shell]

#### [MODIFY] [MainShell.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/shell/MainShell.kt)
- Adjust `Scaffold` configuration to ensure standard bottom bar behavior.
- Ensure `Taskbar` uses `wrapContentHeight()` or similar constraints to prevent it from occupying the full screen if `fillMaxWidth()` is used.

### [Home Screen]

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Update `HomeTileItem` to load real app icons using `PackageManager`.
- Use `AsyncImage` for efficient icon rendering.
- Maintain the sharp content over blurred background via `FluentSurface`.

## Verification Plan

### Automated Tests
- Run existing unit tests: `./gradlew :app:testDebugUnitTest`
- Run instrumented tests: `./gradlew :app:connectedDebugAndroidTest`

### Manual Verification
- Deploy to device/emulator.
- Verify Taskbar is at the bottom and centered.
- Verify Home screen tiles display actual installed app icons (Settings, Gmail, etc.).
- Verify clicking a tile launches the app.
- Verify long-pressing a tile shows the context menu and resizing works.
