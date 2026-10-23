# SHREE REEMPU DEVI TV

A black-and-gold Android TV / Google TV entertainment discovery app, plus a separate private cloud-video backend.

## Existing Android TV app

- Android TV launcher entry point and landscape layout
- Animated opening screen using the supplied app artwork
- Black-and-gold UI with D-pad focus styling
- Official-source discovery categories for news, sports, devotional content, cartoons, anime, learning, music and movie providers
- On-device **My List** for saved official-source shortcuts
- GitHub Actions builds a debug APK artifact

The old TMDB Movie Explorer has been removed. This project does not use TMDB API keys.

## Private cloud movie library

The new `stream.py` service gives you a private personal video library backed by a **private Cloudflare R2 bucket**. You can open the service's web page on your phone, choose a video and upload it directly to cloud storage. Open the same page on another device, enter the same private access token, and stream or delete videos. The service generates short-lived signed upload/play URLs; it does not make your bucket public.

**Important:** this commit adds the cloud backend and a mobile-friendly browser upload/playback page. It does **not yet wire the native Android TV app's menus/player directly to the cloud API**. Until that native integration is added, use the deployed cloud page in a phone/TV browser. The existing APK workflow builds the Android app only, not the Python service.

### 1. Create your cloud bucket

1. Create/sign into a Cloudflare account and open **R2 Object Storage**.
2. Create a bucket, for example `shree-reempu-private`. Keep it private; do not enable public access.
3. Create an R2 API token/key scoped to that bucket with Object Read and Object Write permissions. Copy the Access Key ID and Secret Access Key once.
4. In the bucket's **Settings → CORS Policy**, add a rule that allows your deployed service's origin (for example `https://shree-reempu-cloud.onrender.com`) to use `PUT` and request header `Content-Type`. If you use a custom domain, allow that exact origin too. Do not use a wildcard origin for a private deployment.
5. Find your Cloudflare Account ID.

Cloudflare R2 docs: https://developers.cloudflare.com/r2/

### 2. Deploy the backend on Render

1. Open Render and choose **New → Blueprint**.
2. Connect this GitHub repository and let Render read `render.yaml`.
3. Create the service. In its Environment settings, add these values:
   - `R2_ACCOUNT_ID` — your Cloudflare Account ID
   - `R2_ACCESS_KEY_ID` — the R2 access key ID
   - `R2_SECRET_ACCESS_KEY` — the R2 secret access key
   - `R2_BUCKET_NAME` — your private bucket name
   - `STREAM_API_TOKEN` — a unique random secret with at least 32 characters. Never commit or share this value.
4. Save and deploy. Open `https://YOUR-SERVICE.onrender.com/health`; it should report `ok: true` and `storageConfigured: true`.
5. Open the service root URL on your phone. Enter the same `STREAM_API_TOKEN`, connect, and upload a test video.

Do not paste cloud credentials or your private token into public issues, source files, screenshots, or chat messages. Keep the token only on devices you trust.

### 3. How uploads and playback work

- The phone requests a short-lived upload URL from `stream.py`.
- The video file uploads directly from the phone to your private R2 bucket, rather than being stored permanently on the phone or backend server.
- The TV/other device fetches the library list and gets a time-limited play URL. Playback is streamed from cloud storage; it is not intended to download a second full copy.
- You can delete uploaded objects from the library page.
- Current direct-upload limit is configured by `MAX_VIDEO_BYTES` (default 5,000,000,000 bytes). This version uses a single signed PUT, not resumable multipart uploads.
- For widest TV compatibility, MP4 with H.264 video and AAC audio is recommended. Other formats may not play on every device. This version streams original files and does not transcode them to HLS.

### 4. Local run (optional)

Python 3.11+ is recommended.

```bash
pip install -r requirements.txt
# Set the environment variables from .env.example in your shell, then:
uvicorn stream:app --host 0.0.0.0 --port 8000
```

Do not put real credentials in `.env.example`; it contains placeholders only. The actual `.env` file should never be committed.

### API overview

- `GET /health` — public health/configuration status (does not expose secrets)
- `GET /` — phone-friendly private cloud page
- `GET /api/videos` — list the library (Bearer token required)
- `POST /api/uploads` — request a short-lived signed upload URL
- `POST /api/uploads/{video_id}/complete` — verify upload completion
- `GET /api/videos/{video_id}/play` — redirect to a short-lived signed playback URL
- `DELETE /api/videos/{video_id}` — delete a cloud video

All library/upload/delete endpoints require `Authorization: Bearer YOUR_STREAM_API_TOKEN`. Keep the R2 bucket private. Only upload videos you own or are authorized to store and stream.

## Install the Android TV APK

1. Open the repository's **Actions** tab.
2. Select **Build INSaNe Gold TV APK** and open the latest successful run.
3. Download the `INSaNe-Gold-TV-debug-apk` artifact ZIP and extract `app-debug.apk`.
4. Transfer the APK to the TV and install it using a trusted sideloading method.

## Current limitations

- Cloudflare R2 and Render accounts, bucket, credentials, and service environment variables must be configured by you; GitHub cannot create these accounts or secret values for you.
- Native in-app Android TV cloud browsing/playback and Android phone picker integration are not wired yet; the included browser page is the first working client.
- Uploads currently use a single signed PUT. Large or interrupted uploads do not resume automatically.
- Original videos are streamed without transcoding. Device codec support varies.
- Cloud storage and bandwidth can cost money. Check current provider pricing before uploading a large library.
- This is a private personal library, not a public movie-hosting service.

## Project details

- Android package: `com.insane.goldtv`
- Android TV screen: `app/src/main/java/com/insane/goldtv/MainActivity.kt`
- Cloud backend: `stream.py`
- Python dependencies: `requirements.txt`
- Render Blueprint: `render.yaml`
