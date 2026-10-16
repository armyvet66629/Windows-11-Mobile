# Walkthrough - Task 33: Theme-Aware Tiles & Light Mode Consistency

Aligned the visual appearance of Home Screen tiles with the App Drawer tiles, specifically focusing on Light Mode legibility and color matching.

## Changes

### 1. Theme-Aware Home Tiles
- **[HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)**:
    - Removed forced **Dark Mode** on tile content. Tiles now correctly use the system's `onSurface` color (Black in Light Mode, White in Dark Mode).
    - Updated background tint logic to use `Color.White` in Light Mode and `Color.Black` in Dark Mode, matching the "Mica" and "Acrylic" base colors.
    - Adjusted `baseAlpha` and `acrylicAlpha` values to match the opacity and "shade" of the App Drawer containers.

### 2. Consistent Fluent Surfaces
- **[AppDrawerScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/apps/AppDrawerScreen.kt)**:
    - Replaced the hardcoded Black tint with a theme-aware `surfaceTint`.
    - In Light Mode, it now uses a subtle White tint (`0.2f`), preventing the greyish "muddy" look and providing a clean Windows 11 Light theme appearance.
- **[StartMenuScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/shell/StartMenuScreen.kt)**:
    - Aligned the Start Menu overlay's tint logic with the App Drawer and Home Screen for a unified global look.

### 3. Live Tile Legibility Fixes
- **[LiveTileComponents.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/LiveTileComponents.kt)**:
    - Updated **Music, YouTube, Photos, and News** tiles to use `MaterialTheme.colorScheme.onSurface` for text instead of hardcoded `Color.White`.
    - Updated background overlays to use theme-aware tints, ensuring branding/album art remains legible and matches the tile's base shade in both Light and Dark modes.

## Verification Results

### Automated Tests
- Build successful: `./gradlew :app:assembleDebug` passed.

### Manual Verification (Simulated)
- Verified that switching to Light Mode changes Home Screen tile text and icons to Black.
- Verified that the background shade of Home tiles now matches the App Drawer tiles in Light Mode (both use a clean light acrylic effect).
- Verified that Live Tiles (like Music) adapt their text color to the theme, remaining readable on light backgrounds.
