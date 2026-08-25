# Walkthrough - Home Screen Tile Legibility Fix

The legibility issue on the Home Screen tiles was caused by applying a blur effect to the entire `FluentSurface` container, which included its children (app icons and text). This has been fixed by isolating the blur effect to a separate background layer.

## Changes

### 1. Isolated Background Blur
In [FluentSurface.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/components/FluentSurface.kt), the `Box` structure was refactored into three distinct layers:
- **Background Layer**: Applies the `Modifier.blur()` and the acrylic gradient.
- **Border Layer**: Renders a sharp, high-contrast border for better definition.
- **Content Layer**: Renders the icons and text without any blur effect, ensuring perfect sharpness.

### 2. Improved Contrast
- Increased the default alpha values for the acrylic effect to ensure text stands out against varied backgrounds.
- Specifically adjusted the `HomeTileItem` in [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt) to use `alpha = 0.4f` and a slightly larger `blurRadius`.
- Updated `AppDrawerScreen.kt` to also use higher alpha for better legibility in the list view.

## Verification Results

### Automated Tests
- Build successful: `:app:assembleDebug` passed.

### Manual Verification
- Icons and labels are now rendered in a separate layer from the blur effect, making them sharp and legible.
- The Acrylic effect remains visible in the background, maintaining the Windows 11 aesthetic.
