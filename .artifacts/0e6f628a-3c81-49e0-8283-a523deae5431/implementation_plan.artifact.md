# Fix Legibility Issue on Home Screen Tiles

The Home Screen tiles are currently blurry because the `Modifier.blur()` is applied to the entire `FluentSurface` container, which includes the foreground content (icons and text). This plan aims to isolate the blur effect to the background layer and improve contrast for better legibility.

## User Review Required

> [!NOTE]
> The background blur effect (Acrylic) will now only affect the background layer of the tiles. Icons and text will remain perfectly sharp.

## Proposed Changes

### UI Components

#### [MODIFY] [FluentSurface.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/components/FluentSurface.kt)
- Restructure the `Box` to use a layered approach.
- Move `Modifier.blur()` to a background-only `Box`.
- Ensure the foreground `content()` is rendered in a separate layer above the blurred background.
- Adjust default `alpha` and `color` handling to ensure high contrast.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Adjust the `HomeTileItem` parameters if necessary to improve contrast (e.g., slightly higher alpha for the acrylic effect).

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project still builds.

### Manual Verification
- Visual inspection of the Home Screen tiles in the IDE preview (if possible) or by the user.
- Verify that icons and text are sharp.
- Verify that the background still has the translucent/acrylic feel.
