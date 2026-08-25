# Implementation Plan - Dark Mode and Legibility Fixes

Fix Dark Mode UI issues in the Windows 11 Mobile Launcher, focusing on system bar tinting and text legibility.

## Proposed Changes

### [Component Name] Theme & System UI

#### [MODIFY] [Theme.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/theme/Theme.kt)
- Update `Windows11MobileTheme` to correctly handle navigation bar icon tinting using `isAppearanceLightNavigationBars`.
- Ensure the `SideEffect` correctly applies to the window when the theme changes.
- Add `dynamicColor` parameter check to ensure it doesn't override Fluent design intent if not desired (optional, but keep it for now as it's M3 standard).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/MainActivity.kt)
- Review `enableEdgeToEdge()` usage. The current implementation in `Theme.kt` might be redundant or conflicting with `enableEdgeToEdge()`. I'll align them to ensure transparency and correct tinting.

### [Component Name] Components & Legibility

#### [MODIFY] [FluentSurface.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/components/FluentSurface.kt)
- Wrap content in `CompositionLocalProvider(LocalContentColor provides contentColor)` to ensure text and icons inside the surface use the correct color token.
- Add a `contentColor` parameter that defaults to `contentColorFor(color)`.
- Adjust the "acrylic" tinting layers for dark mode to improve contrast.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Ensure all `Text` and `Icon` components use `MaterialTheme.colorScheme` tokens.
- Specifically check the tile labels and context menu items.

#### [MODIFY] [AppDrawerScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/apps/AppDrawerScreen.kt)
- Review search bar colors to ensure they are legible in dark mode.
- Ensure app names and package names use theme tokens.

## Verification Plan

### Automated Tests
- N/A (UI changes)

### Manual Verification
- Toggle between Light and Dark mode in the app settings (or system settings).
- Verify status bar and navigation bar icons change color (e.g., white icons in dark mode, dark icons in light mode).
- Verify all text in the Home Screen (tile labels) and App Drawer (app names, section headers) is clearly visible.
- Verify the Taskbar (dock) icons are visible and the background adapts correctly.
