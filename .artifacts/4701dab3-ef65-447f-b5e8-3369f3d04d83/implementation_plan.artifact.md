# Implementation Plan - Refine Live Tiles with Mica/Acrylic Theme

Refine the Fluent Design implementation in the Windows 11 Mobile app by enhancing `FluentSurface` with high-fidelity Mica and Acrylic effects, and applying these to the Home Screen, App Drawer, and News Pane.

## User Review Required

> [!IMPORTANT]
> The `FluentSurface` API will be updated to include an `effect` parameter. Existing calls to `FluentSurface` will be updated to use either `MICA` or `ACRYLIC` based on their context.

## Proposed Changes

### UI Components

#### [MODIFY] [FluentSurface.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/components/FluentSurface.kt)
- Add `FluentEffect` enum (`MICA`, `ACRYLIC`).
- Update `FluentSurface` signature:
    - Add `effect: FluentEffect = FluentEffect.ACRYLIC`.
    - Refine default `blurRadius` and `alpha`.
- Implement **Mica** logic:
    - Background-aware tint using a vertical gradient (light top, dark bottom).
    - Reduced blur compared to Acrylic.
    - Subtle color pick-up from the background.
- Implement **Acrylic** logic:
    - Deeper blur.
    - Refined noise texture (smaller grains, more subtle).
- Refine "Glass" border:
    - Use a multi-stop gradient for the border to simulate light reflection on edges.
- Ensure `tileOpacity` (via `alpha` parameter) is correctly integrated.

### Home Screen

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Update `HomeTileItem` to use `FluentEffect.MICA`.
- Update `HomeHeader` to use `FluentEffect.ACRYLIC` or `MICA` as appropriate (Header likely Mica for solidity).
- Ensure `notificationSummary` and `notificationCount` badge colors are sharp and legible against the new surfaces.

### App Drawer

#### [MODIFY] [AppDrawerScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/apps/AppDrawerScreen.kt)
- Update `AppItem` and `LauncherSettingsItem` to use the refined `FluentSurface`.
- Use `FluentEffect.ACRYLIC` for a modern flyout feel.

### News Pane

#### [MODIFY] [NewsFeedScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/news/NewsFeedScreen.kt)
- Update `NewsCard` to use `FluentEffect.ACRYLIC`.
- Ensure text legibility on top of the blurred background.

## Verification Plan

### Automated Tests
- Build the project: `./gradlew assembleDebug`
- Run unit tests (if any relevant): `./gradlew testDebugUnitTest`

### Manual Verification
- Verify the Mica effect on Home Screen tiles: check for subtle vertical gradient and background tint pick-up.
- Verify the Acrylic effect on News Cards and App Drawer items: check for deep blur and subtle noise texture.
- Verify the "Glass" border on all refined components.
- Check that Tile Opacity settings still work in the Home Screen.
- Ensure text and icons remain sharp across all themes.
