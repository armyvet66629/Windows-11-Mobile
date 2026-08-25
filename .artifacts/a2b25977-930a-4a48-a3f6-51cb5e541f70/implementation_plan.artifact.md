# Implementation Plan - Windows 11 Mobile Interactive Context Menus and Centered Taskbar

This plan outlines the steps to implement a Windows 11 style context menu for tiles and a centered, fluent taskbar.

## User Review Required

> [!IMPORTANT]
> The context menu will be implemented as a custom popup to mimic the Windows 11 style. The taskbar will be centered at the bottom of the screen.

## Proposed Changes

### UI Components

#### [MODIFY] [MainShell.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/shell/MainShell.kt)
- Redesign the `Taskbar` to be centered and include pinned app icons and the Start button.
- Make the taskbar reactive to the navigation state (e.g., highlighting the active app or changing visibility).

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/home/HomeScreen.kt)
- Replace `ModalBottomSheet` with a `DropdownMenu` or custom popup for the Windows 11 style context menu.
- Add options for resizing, "Uninstall", and "App Info".
- Implement Intents for Uninstall and App Info.

#### [NEW] [TaskbarViewModel.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/ui/shell/TaskbarViewModel.kt)
- Manage pinned apps and taskbar state.

### Data

#### [MODIFY] [AppRepository.kt](file:///C:/Users/hawks/AndroidStudioProjects/Windows11Mobile/app/src/main/java/com/example/windows11mobile/data/AppRepository.kt)
- Add a method to get specific app info by package name (optional, if needed).

## Verification Plan

### Automated Tests
- Build the project: `./gradlew :app:assembleDebug`

### Manual Verification
- Long-press a tile and verify the context menu appears with correct options.
- Test resizing tiles.
- Test "App Info" and "Uninstall" options.
- Verify the taskbar is centered and contains icons.
- Verify the taskbar button opens the App Drawer.
