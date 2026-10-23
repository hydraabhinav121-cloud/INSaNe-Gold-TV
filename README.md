# INSaNe Gold TV

A free-to-install Android TV / Google TV entertainment discovery hub with a charcoal-black and gold interface designed for TV remotes.

## Included
- Android TV launcher entry point and landscape layout
- Black-and-gold interface with D-pad focus styling
- Search across channel/source names and descriptions
- Categories for news, live-TV discovery, sports, movies, cartoons and anime
- Persistent **My List** favourites stored on the device
- Opens official provider/channel pages using Android's supported app/browser handler
- Internet permission for opening online content
- GitHub Actions workflow that builds a debug APK

## Content and costs
The app itself has no paid subscription or in-app purchase. It links to public-facing official sources, not pirated feeds. Provider availability, ads, accounts, region restrictions and subscription requirements are controlled by each provider. A channel listed here is a discovery shortcut, not a guarantee that its full live feed or every title is free. The Internet Archive includes user-submitted material, so check rights and availability for each film.

## Install the APK
1. Open the repository's **Actions** tab.
2. Select **Build INSaNe Gold TV APK** and open the latest successful run.
3. Download the `INSaNe-Gold-TV-debug-apk` artifact ZIP.
4. Extract it to get `app-debug.apk`.
5. Transfer the APK to your Sony Bravia Google TV and install it using a trusted file manager/sideload method. Only install APKs you trust.

## Current limitations
- Video is opened through the provider's official app or browser; this project does not host or embed a custom streaming player.
- The app cannot unlock paid channels or bypass provider logins, subscriptions or geographic restrictions.
- The search field searches the catalogue built into the app, not the entire internet.
- Google TV remote voice search is controlled by Google TV. This build does not yet register as a full system-wide voice-search provider.
- The listed official channel URLs should be checked periodically because providers can change handles or availability.

## Project
- Package: `com.insane.goldtv`
- Minimum Android version: Android 6.0 (API 23)
- Target SDK: 28 (review and update before any store release)
