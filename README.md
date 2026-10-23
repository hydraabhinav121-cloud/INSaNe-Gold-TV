# SHREE REEMPU DEVI TV

A black-and-gold Android TV / Google TV entertainment discovery app designed for a TV remote. The app preserves the official-source channel guide and adds an optional TMDB-powered movie and TV-series discovery screen.

## Features

- Android TV launcher entry point and landscape layout
- Animated opening screen using the supplied app artwork
- Black-and-gold UI with D-pad focus styling
- Official-source discovery categories for news, sports, devotional content, cartoons, anime, learning, music and movie providers
- On-device **My List** for saved official-source shortcuts
- **Movie Explorer** with trending movies, TV series, family films, animation, poster art, ratings, summaries and search
- Movie cards link to TMDB's watch-provider information and YouTube trailer search; they do not extract or host video streams
- GitHub Actions builds a debug APK artifact

## Enable Movie Explorer metadata

The movie catalogue uses the official TMDB API for metadata and poster paths. A TMDB API key is optional for compiling the app, but the Movie Explorer will show setup instructions until a key is configured.

1. Create/sign into a TMDB account and request an API key (the **API Key (v3 auth)**).
2. Open this GitHub repository → **Settings** → **Secrets and variables** → **Actions**.
3. Select **New repository secret**.
4. Set the secret name to `TMDB_API_KEY` and paste the v3 API key as the secret value.
5. Open **Actions** → **Build INSaNe Gold TV APK** → **Run workflow**, or make a commit to `main).
6. Download the `INSaNe-Gold-TV-debug-apk` artifact from the successful run.

The workflow passes the secret into Gradle at build time. As with any mobile app, a key included in an APK can be extracted; do not treat a client-side key as a private server credential. Use any TMDB key only in accordance with TMDB's current terms, attribution requirements, and applicable commercial-use rules. The app displays a TMDB attribution notice.

Official documentation: https://developer.themoviedb.org/docs/getting-started

## Install the APK on Sony Bravia / Google TV

1. Open the repository's **Actions** tab.
2. Select **Build INSaNe Gold TV APK** and open the latest successful run.
3. Download the `INSaNe-Gold-TV-debug-apk` artifact ZIP and extract `app-debug.apk`.
4. Transfer the APK to the TV and install it using a trusted sideloading method. Only install APKs you trust.

## Content, playback and rights

- SHREE REEMPU DEVI TV is a discovery/shortcut app; it does not host films or provide a custom movie-streaming player.
- Movie Explorer shows metadata and links to TMDB's watch options and YouTube trailer search. Actual playback is handled by the linked provider, subject to availability in India, provider rules, account requirements and subscription status.
- A listed provider or search shortcut does not guarantee a title is free, family-friendly, available in India, or embeddable. Check each title and provider.
- The Internet Archive may contain user-submitted items. Check the rights statement for each item before watching or redistributing.
- This project does **not** integrate MovieBox private authentication, session tokens, undocumented endpoints, raw stream extraction, CDN proxying or downloading.
- No Vercel backend is required for the current design. The Android build and APK artifact are handled by GitHub Actions.

## Current limitations

- Movie Explorer requires a TMDB v3 API key to fetch live metadata. Without it, the existing source catalogue still works.
- The app does not unlock paid services, bypass logins, remove ads or bypass regional restrictions.
- The existing source search filters the app's own source list; Movie Explorer search searches TMDB.
- Google TV voice search is controlled by Google TV; this app does not register as a system-wide voice-search provider.
- Provider URLs and availability can change over time and may need maintenance.

## Project details

- Package: `com.insane.goldtv`
- Minimum Android version: Android 6.0 (API 23)
- Target SDK: 28 (review and update before any app-store release)
- Main screen: `app/src/main/java/com/insane/goldtv/MainActivity.kt`
- Movie discovery: `app/src/main/java/com/insane/goldtv/MovieExplorer.kt`
