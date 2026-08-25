# Implementation Plan - Fix Home Screen Clock and Weather Contrast

Improve the legibility of the clock and weather information on the Home Screen by wrapping it in a subtle `FluentSurface` with a glass effect.

## User Review Required

> [!NOTE]
> The `FluentSurface` will have a subtle alpha (0.15f) and blur to provide contrast against various wallpapers while maintaining the Windows 11 Acrylic aesthetic.

## Proposed Changes

### Home Screen

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Update `HomeHeader` composable to wrap the clock and weather content in a `FluentSurface`.
- Adjust padding and alignment to ensure the container is centered and sized appropriately.
- Update text colors to use `onSurface` (via `LocalContentColor`) to match the surface container.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build passes.

### Manual Verification
- The UI can be verified using the `@Preview` functions in `HomeScreen.kt`.
