# INSaNe Gold TV

An Android TV / Google TV starter app with a charcoal-black and gold interface designed for a TV remote.

## Included
- Android TV launcher entry point
- Gold-themed home screen and selectable category tiles
- D-pad focus styling for TV remote navigation
- GitHub Actions workflow to build a debug APK

## Build an APK
1. Open the repository's **Actions** tab.
2. Select **Build INSaNe Gold TV APK**.
3. Click **Run workflow** (or push a commit to `main`).
4. Open the completed workflow run and download the `INSaNe-Gold-TV-debug-apk` artifact.
5. Extract the artifact ZIP to get `app-debug.apk`.

## Important
This is a starter UI, not a finished streaming service. Real video playback, licensed channel feeds, catalogue search, voice-search integration, and favourites persistence are not implemented yet. Add only content sources you are authorised to use; paid channels and movies may require subscriptions or distribution rights. Google TV voice input is controlled by Android/Google TV and is not guaranteed to launch in-app search automatically.

## Project
- Package: `com.insane.goldtv`
- Minimum Android version: Android 6.0 (API 23)
- Target SDK: 28 (chosen for broad sideload compatibility in this starter; review and update before any store release)
