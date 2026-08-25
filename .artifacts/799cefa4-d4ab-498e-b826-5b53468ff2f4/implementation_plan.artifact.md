# Implementation Plan - Swipe Navigation & Enhanced Fluent Acrylic Theme

Implement a swipe-based navigation system for the main shell, featuring a News Feed and an integrated App Drawer. Additionally, enhance the `FluentSurface` to better capture the Windows 11 Acrylic/Mica aesthetic with noise textures and refined tinting.

## User Review Required

> [!IMPORTANT]
> The App Drawer will be integrated into the main `HorizontalPager` on the Desktop. This may change how the "Start" button in the taskbar behaves (it should ideally scroll the pager to the App Drawer page or trigger a specific navigation).

## Proposed Changes

### Navigation & Shell

#### [MODIFY] [MainShell.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/shell/MainShell.kt)
- Integrate `androidx.compose.foundation.pager.HorizontalPager`.
- Update the `Dest.Desktop` navigation entry to host a `HorizontalPager` with three pages:
    - **Page 0**: App Drawer (Left swipe from Home).
    - **Page 1**: Home Screen (Center).
    - **Page 2**: News Feed (Right swipe from Home).
- Synchronize the Taskbar's "Start" button to scroll to Page 0.
- Handle Edge-to-Edge insets for the Pager content.

#### [MODIFY] [NavKey.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/navigation/NavKey.kt)
- Add `Dest.NewsFeed` to the `Dest` sealed interface if needed for deep linking, though primarily it will be a page in the Pager.

---

### New Features

#### [NEW] [NewsFeedScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/news/NewsFeedScreen.kt)
- Mimic the Windows 11 Widgets panel.
- Implementation details:
    - Blurred background using `FluentSurface`.
    - A vertical list of cards representing news/widgets.
    - Integration with a placeholder repository for news data.
    - Search bar at the top with Fluent styling.

---

### UI Components

#### [MODIFY] [FluentSurface.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/components/FluentSurface.kt)
- **Noise Texture**: Add a noise overlay layer using a tiled noise bitmap to simulate the grain found in Acrylic/Mica materials.
- **Enhanced Tinting**: Refine the gradient and opacity to better simulate light absorption and reflection.
- **Parameters**: Add `noiseOpacity: Float` and `tintColor: Color` for better customization.

#### [NEW] [noise_texture.xml](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/res/drawable/noise_texture.xml)
- A drawable resource (or a small PNG generated via tool) to provide the grain effect.

---

## Verification Plan

### Automated Tests
- N/A for UI-specific visual enhancements, but ensure `AppDrawerViewModel` still functions correctly when hosted in the Pager.

### Manual Verification
1. **Swipe Navigation**:
    - Launch the app.
    - Swipe right from Home to ensure the App Drawer appears.
    - Swipe left from Home to ensure the News Feed appears.
    - Verify smooth transitions and no layout breaks.
2. **Visual Check**:
    - Inspect `FluentSurface` components (like the Taskbar and News Cards) for the noise texture and improved depth.
    - Verify dark and light mode compatibility.
3. **Edge-to-Edge**:
    - Ensure content doesn't overlap with system bars or the taskbar improperly.
