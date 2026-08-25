# Implementation Plan - Task 16: Legibility and System UI Fixes

This plan addresses legibility issues in Light Mode, specifically focusing on hardcoded white text that disappears on light backgrounds, and ensuring the status bar adaptively changes its icon/text color based on the theme.

## Proposed Changes

### Core UI Framework

#### [MODIFY] [MainActivity.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/MainActivity.kt)
- Update `enableEdgeToEdge()` to correctly handle status bar styles based on the theme state from `SettingsRepository`.

#### [MODIFY] [Theme.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/theme/Theme.kt)
- Refine `SideEffect` logic to ensure consistent application of `isAppearanceLightStatusBars` and `isAppearanceLightNavigationBars`.

#### [MODIFY] [Color.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/theme/Color.kt)
- Review and adjust color tokens for optimal contrast.

### Feature Screens (Legibility Fixes)

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/settings/SettingsScreen.kt)
- Replace hardcoded `Color.White` with `MaterialTheme.colorScheme.onSurface` or `onBackground` for all headers, labels, and icons.
- Remove explicit `contentColor = Color.White` from `FluentSurface` calls to allow it to use the theme-aware default.

#### [MODIFY] [AppDrawerScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/apps/AppDrawerScreen.kt)
- Update `OutlinedTextField` (Search Bar) to use theme-aware colors for text, placeholder, icons, and borders.
- Replace hardcoded `Color.White` in `LauncherSettingsItem` and `AppItem`.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Update `HomeTileItem` to use theme-aware colors for labels and ripples.
- Update `TileContextMenu` to use theme-aware colors.

#### [MODIFY] [NewsFeedScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/news/NewsFeedScreen.kt)
- Ensure all headers and card text use proper color tokens.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project builds correctly.

### Manual Verification
- Verify that in Light Mode:
    - Status bar icons (clock, battery) are dark.
    - All text in Settings, App Drawer, and Home Screen is visible (black/dark grey).
    - The search bar in App Drawer is clearly visible.
- Verify that in Dark Mode:
    - Status bar icons are light.
    - All text remains visible (white).
