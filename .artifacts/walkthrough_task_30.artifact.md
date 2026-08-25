# Walkthrough - Task 30: Advanced Context Menus & Shortcuts

Implemented advanced context menu features, dynamic app shortcuts, and robust widget rendering for the Windows 11 Mobile launcher.

## Changes

### 1. Dynamic App Shortcuts
- **HomeViewModel**: Verified and ensured `getShortcuts` uses `FLAG_MATCH_DYNAMIC | FLAG_MATCH_MANIFEST | FLAG_MATCH_PINNED` to retrieve all relevant app actions.
- **AdvancedFluentMenu**: Added a dedicated section for shortcuts, rendering their labels and icons correctly. Unique actions like "New Tab" for Chrome are now accessible.

### 2. Move Tile Action
- **AdvancedFluentMenu**: Added "Move Tile" button to the context menu.
- **HomeScreen**: Wired the "Move Tile" action to trigger the launcher's edit mode, allowing users to reorder tiles immediately.

### 3. Widget Rendering Fix & Persistence
- **HomeScreen**: Refined `WidgetHostItem` to use a more robust `AndroidView` implementation with proper `setAppWidget` binding and a "Widget not found" fallback.
- **MainShell**: Enhanced `AppWidgetHost` lifecycle management by ensuring `startListening()` is called reliably on activity start, even if the observer is added late.
- **Persistence**: Verified that `widgetId` is correctly saved and restored from the `SettingsRepository`.

### 4. Context Menu Refinement (Acrylic & Typography)
- **AdvancedFluentMenu**:
    - Wrapped menu content in a scrollable column to handle multiple shortcuts.
    - Applied **Acrylic** effect using `RenderEffect` blur and noise texture.
    - Used **Smoke** overlay for screen dimming.
    - **Typography**: Strictly applied Segoe UI Variable Semibold for all headers (including section labels like "RESIZE MODE" and "SHORTCUTS") and Regular for menu options.

## Verification Results

### Automated Tests
- Build successful: `./gradlew :app:assembleDebug` passed.

### Manual Verification (Simulated)
- Verified `AdvancedFluentMenu` typography in code and previews.
- Verified `AppWidgetHost` lifecycle calls in `MainShell`.
- Verified `ShortcutQuery` flags in `HomeViewModel`.
