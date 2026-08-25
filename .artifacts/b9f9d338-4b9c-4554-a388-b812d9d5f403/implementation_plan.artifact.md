# Implementation Plan - Dynamic Context Menus and Widget Integration Refinement

Refine the Dynamic Context Menus and Widget Integration in the Windows 11 Mobile Launcher to ensure they are contextual and functional.

## User Review Required

> [!IMPORTANT]
> The app needs `android:usesCleartextTraffic="true"` to load news feeds from non-HTTPS sources. This is a security trade-off for content availability.

## Proposed Changes

### [Network Security]
#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/AndroidManifest.xml)
- Add `android:usesCleartextTraffic="true"` to the `<application>` tag.

### [Home Screen & View Model]
#### [MODIFY] [HomeViewModel.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeViewModel.kt)
- Expose the `AppWidgetHost` instance so it can be shared with `WidgetHostItem`.
- Refine shortcut fetching to ensure it works across different Android versions (N_MR1+).

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Update `WidgetHostItem` to use the shared `AppWidgetHost` from `HomeViewModel`.
- Fix the widget addition flow:
    - Handle widget configuration activity if required.
    - Properly bind the widget ID.
- Implement a distinct long-press menu for the Home Screen background (Widget Picker trigger).
- Improve UI polish: High-contrast headers in Light Mode for `AdvancedFluentMenu`.

### [Components]
#### [MODIFY] [AdvancedFluentMenu.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/components/AdvancedFluentMenu.kt)
- Ensure headers and text have enough contrast in Light Mode.
- Polish the shortcut display with better icons if possible.

## Verification Plan

### Automated Tests
- `gradlew :app:assembleDebug` to ensure compilation.

### Manual Verification
- Long-press an app tile: verify that "New Message" or other app-specific shortcuts appear.
- Click a shortcut: verify it launches the correct activity.
- Long-press background: verify a clear "Home Settings / Add Widget" menu appears.
- Add a widget: verify it appears on the home screen and remains after restart.
- Check news feed: verify articles load even from non-HTTPS sources.
