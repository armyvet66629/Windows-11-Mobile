# Task 23: Advanced Shortcuts and Widget Integration

Implement dynamic app shortcuts, polish the news pane, and add Android App Widget support to the Windows 11 Mobile Launcher.

## User Review Required

> [!IMPORTANT]
> The widget picker will use a custom dialog if I can't trigger the system one easily, but usually, launchers implement their own picker. I will attempt to use the system `ACTION_PICK_APPWIDGET` for a native feel if possible, otherwise, I'll build a simple list.

## Proposed Changes

### Data Model

#### [MODIFY] [HomeTile.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/data/HomeTile.kt)
- Add `widgetId` (Int?) and `isWidget` (Boolean) to `HomeTile` data class.
- Make `packageName` optional if it's a widget.

### Home Screen & ViewModels

#### [MODIFY] [HomeViewModel.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeViewModel.kt)
- Add logic to fetch dynamic shortcuts for a given package name.
- Add methods to handle adding/removing widgets.
- Add `addWidgetTile(widgetId: Int, label: String)` method.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Implement long-press on background to trigger widget picker.
- Update `HomeTileItem` to render `AndroidView` for `AppWidgetHostView` if the tile is a widget.
- Integrate `AppWidgetHost` and `AppWidgetManager`.

### Components

#### [MODIFY] [AdvancedFluentMenu.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/components/AdvancedFluentMenu.kt)
- Add a list of dynamic shortcuts to the menu.
- Add "Check for Updates" action.
- Ensure the menu can launch shortcut intents.

### News Pane

#### [MODIFY] [NewsFeedScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/news/NewsFeedScreen.kt)
- Update `NewsHeader` to include current time (HH:mm).
- Improve Light Mode contrast for headers.

## Verification Plan

### Automated Tests
- N/A (UI focused task, mostly manual verification of system integrations).

### Manual Verification
- Long-press an app tile (e.g., Messages) and verify dynamic shortcuts appear.
- Click "Check for Updates" and verify it opens App Info or Play Store.
- Verify current time in News header.
- Switch to Light Mode and verify header legibility.
- Long-press Home Screen background, add a widget (e.g., Clock or Search), and verify it appears and persists.
