"""
SHREE REEMPU DEVI TV — private personal video cloud.

Storage: Cloudflare R2 (S3-compatible). The bucket stays private; this API creates
short-lived upload/play URLs. Set the environment variables in .env.example.
Only upload videos you own or are authorized to store and stream.
"""
from __future__ import annotations

import html
import os
import re
import secrets
import uuid
from datetime import datetime, timezone
from typing import Any
from urllib.parse import quote

import boto3
from botocore.config import Config
from botocore.exceptions import ClientError
from fastapi import Depends, FastAPI, Header, HTTPException, Request, status
from fastapi.responses import HTMLResponse, JSONResponse, RedirectResponse
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel, Field

APP_NAME = "SHREE REEMPU DEVI TV — My Cloud"
R2_ACCOUNT_ID = os.getenv("R2_ACCOUNT_ID", "").strip()
R2_ACCESS_KEY_ID = os.getenv("R2_ACCESS_KEY_ID", "").strip()
R2_SECRET_ACCESS_KEY = os.getenv("R2_SECRET_ACCESS_KEY", "").strip()
R2_BUCKET_NAME = os.getenv("R2_BUCKET_NAME", "").strip()
STREAM_API_TOKEN = os.getenv("STREAM_API_TOKEN", "").strip()
MAX_VIDEO_BYTES = int(os.getenv("MAX_VIDEO_BYTES", str(20 * 1024 * 1024 * 1024)))
UPLOAD_URL_TTL = 15 * 60
PLAY_URL_TTL = 4 * 60 * 60
ALLOWED_EXTENSIONS = {".mp4", ".m4v", ".mov", ".mkv", ".webm", ".avi", ".mpeg", ".mpg", ".ts"}

app = FastAPI(title=APP_NAME, version="1.0.0")
bearer = HTTPBearer(auto_error=False)


def storage_client():
    if not all((R2_ACCOUNT_ID, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY, R2_BUCKET_NAME)):
        raise HTTPException(status_code=503, detail="Cloud storage is not configured. Set the R2 environment variables.")
    return boto3.client(
        "s3",
        endpoint_url=f"https://{R2_ACCOUNT_ID}.r2.cloudflarestorage.com",
        aws_access_key_id=R2_ACCESS_KEY_ID,
        aws_secret_access_key=R2_SECRET_ACCESS_KEY,
        region_name="auto",
        config=Config(signature_version="s3v4", s3={"addressing_style": "path"}),
    )


def require_token(credentials: HTTPAuthorizationCredentials | None = Depends(bearer)) -> str:
    if not STREAM_API_TOKEN or len(STREAM_API_TOKEN) < 32:
        raise HTTPException(status_code=503, detail="Set STREAM_API_TOKEN to a random secret of at least 32 characters.")
    supplied = credentials.credentials if credentials else ""
    if not secrets.compare_digest(supplied, STREAM_API_TOKEN):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Enter your private cloud access token.",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return supplied


def safe_filename(value: str) -> str:
    # Keep a plain filename; never let a client choose a storage path.
    value = value.replace("\\", "/").split("/")[-1].strip()
    value = re.sub(r"[^A-Za-z0-9._() -]", "_", value)
    value = re.sub(r"\s+", " ", value).strip(" .")
    return (value or "video.mp4")[:150]


def video_id_from_key(key: str) -> str | None:
    parts = key.split("/", 2)
    return parts[1] if len(parts) == 3 and parts[0] == "library" else None


def find_video(client: Any, video_id: str) -> dict[str, Any]:
    if not re.fullmatch(r"[0-9a-f]{32}", video_id):
        raise HTTPException(status_code=404, detail="Video not found.")
    response = client.list_objects_v2(Bucket=R2_BUCKET_NAME, Prefix=f"library/{video_id}/", MaxKeys=2)
    objects = response.get("Contents", [])
    if not objects:
        raise HTTPException(status_code=404, detail="Video not found.")
    item = objects[0]
    key = item["Key"]
    filename = key.split("/", 2)[-1]
    ext = os.path.splitext(filename)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(status_code=404, detail="Video not found.")
    return {
        "id": video_id,
        "key": key,
        "title": os.path.splitext(filename)[0],
        "filename": filename,
        "sizeBytes": int(item.get("Size", 0)),
        "updatedAt": item.get("LastModified", datetime.now(timezone.utc)).isoformat(),
        "contentType": "video/mp4" if ext in {".mp4", ".m4v"} else "video/" + ext.lstrip("."),
    }


class UploadRequest(BaseModel):
    filename: str = Field(min_length=1, max_length=300)
    title: str | None = Field(default=None, max_length=150)
    contentType: str = Field(default="video/mp4", max_length=100)
    sizeBytes: int = Field(gt=0)


class CompleteRequest(BaseModel):
    pass


@app.get("/health")
def health():
    return {"ok": True, "service": APP_NAME, "storageConfigured": bool(
        R2_ACCOUNT_ID and R2_ACCESS_KEY_ID and R2_SECRET_ACCESS_KEY and R2_BUCKET_NAME
    )}


@app.get("/", response_class=HTMLResponse)
def home():
    return HTMLResponse(PAGE)


@app.get("/api/videos")
def list_videos(_: str = Depends(require_token)):
    client = storage_client()
    items: list[dict[str, Any]] = []
    try:
        paginator = client.get_paginator("list_objects_v2")
        for page in paginator.paginate(Bucket=R2_BUCKET_NAME, Prefix="library/"):
            for obj in page.get("Contents", []):
                key = obj["Key"]
                video_id = video_id_from_key(key)
                filename = key.split("/", 2)[-1]
                if not video_id or os.path.splitext(filename)[1].lower() not in ALLOWED_EXTENSIONS:
                    continue
                title = os.path.splitext(filename)[0]
                items.append({
                    "id": video_id,
                    "title": title,
                    "filename": filename,
                    "sizeBytes": int(obj.get("Size", 0)),
                    "updatedAt": obj.get("LastModified", datetime.now(timezone.utc)).isoformat(),
                    "playUrl": client.generate_presigned_url(
                        "get_object",
                        Params={"Bucket": R2_BUCKET_NAME, "Key": key},
                        ExpiresIn=PLAY_URL_TTL,
                    ),
                })
        items.sort(key=lambda item: item["updatedAt"], reverse=True)
        return {"videos": items, "count": len(items)}
    except ClientError:
        raise HTTPException(status_code=502, detail="Could not read your cloud library. Check the R2 bucket and credentials.")


@app.post("/api/uploads")
def create_upload(body: UploadRequest, _: str = Depends(require_token)):
    filename = safe_filename(body.filename)
    extension = os.path.splitext(filename)[1].lower()
    if extension not in ALLOWED_EXTENSIONS:
        raise HTTPException(status_code=415, detail="Unsupported video file type.")
    if body.sizeBytes > MAX_VIDEO_BYTES:
        raise HTTPException(status_code=413, detail=f"Video exceeds configured limit ({MAX_VIDEO_BYTES} bytes).")
    if body.contentType and not body.contentType.startswith("video/") and body.contentType != "application/octet-stream":
        raise HTTPException(status_code=415, detail="Only video uploads are accepted.")
    client = storage_client()
    video_id = uuid.uuid4().hex
    key = f"library/{video_id}/{filename}"
    content_type = body.contentType if body.contentType.startswith("video/") else "application/octet-stream"
    try:
        url = client.generate_presigned_url(
            "put_object",
            Params={"Bucket": R2_BUCKET_NAME, "Key": key, "ContentType": content_type},
            ExpiresIn=UPLOAD_URL_TTL,
        )
        return {
            "videoId": video_id,
            "objectKey": key,
            "uploadUrl": url,
            "contentType": content_type,
            "expiresInSeconds": UPLOAD_URL_TTL,
        }
    except ClientError:
        raise HTTPException(status_code=502, detail="Could not prepare the cloud upload.")


@app.post("/api/uploads/{video_id}/complete")
def complete_upload(video_id: str, _: str = Depends(require_token)):
    client = storage_client()
    if not re.fullmatch(r"[0-9a-f]{32}", video_id):
        raise HTTPException(status_code=404, detail="Upload not found.")
    response = client.list_objects_v2(Bucket=R2_BUCKET_NAME, Prefix=f"library/{video_id}/", MaxKeys=2)
    objects = response.get("Contents", [])
    if not objects:
        raise HTTPException(status_code=409, detail="Upload has not reached cloud storage yet.")
    item = objects[0]
    if int(item.get("Size", 0)) <= 0:
        raise HTTPException(status_code=400, detail="The uploaded file is empty.")
    return {"ok": True, "video": find_video(client, video_id)}


@app.get("/api/videos/{video_id}/play")
def play_video(video_id: str, _: str = Depends(require_token)):
    client = storage_client()
    video = find_video(client, video_id)
    url = client.generate_presigned_url(
        "get_object",
        Params={"Bucket": R2_BUCKET_NAME, "Key": video["key"]},
        ExpiresIn=PLAY_URL_TTL,
    )
    # The cloud object remains private. This redirect grants a time-limited read URL.
    return RedirectResponse(url=url, status_code=307)


@app.delete("/api/videos/{video_id}")
def delete_video(video_id: str, _: str = Depends(require_token)):
    client = storage_client()
    video = find_video(client, video_id)
    try:
        client.delete_object(Bucket=R2_BUCKET_NAME, Key=video["key"])
        return {"ok": True, "deletedId": video_id}
    except ClientError:
        raise HTTPException(status_code=502, detail="Could not delete this video from cloud storage.")


PAGE = r"""<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="theme-color" content="#090a0d">
<title>SHREE REEMPU DEVI TV · My Cloud</title>
<style>
:root{color-scheme:dark;--gold:#ffd36a;--bg:#090a0d;--panel:#17191f;--muted:#a7a9b2}
*{box-sizing:border-box}body{margin:0;background:radial-gradient(ellipse at top,#2a210f,var(--bg) 55%);color:#fff;font:16px system-ui,Arial,sans-serif}
main{max-width:1000px;margin:auto;padding:24px}header{display:flex;align-items:center;gap:14px;margin-bottom:24px}
.mark{border:1px solid var(--gold);border-radius:16px;color:var(--gold);font-size:25px;padding:12px}
h1{font-size:clamp(22px,4vw,34px);margin:0}p{color:var(--muted);line-height:1.5}
.card{background:var(--panel);border:1px solid #343037;border-radius:18px;padding:18px;margin:14px 0}
label{display:block;color:var(--gold);font-size:13px;margin:10px 0 6px}
input,button{font:inherit;border-radius:10px;padding:12px;border:1px solid #49434a}
input{width:100%;background:#0c0d11;color:white}button{background:var(--gold);color:#17130a;font-weight:750;cursor:pointer;margin:6px 6px 0 0}
button.secondary{background:#292a30;color:white}button.danger{background:#4b2020;color:#fff}
.row{display:flex;gap:12px;align-items:center;flex-wrap:wrap}.row>*{flex:1}
#status{min-height:24px;color:var(--gold)}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(230px,1fr));gap:12px}
.video{min-width:0}.video strong{display:block;overflow-wrap:anywhere}.size{font-size:12px;color:var(--muted)}
progress{width:100%;accent-color:var(--gold)}video{width:100%;max-height:58vh;background:#000;border-radius:12px}
.hidden{display:none!important}.small{font-size:12px}
</style></head>
<body><main>
<header><div class="mark">✦</div><div><h1>SHREE REEMPU DEVI TV</h1><p style="margin:4px 0">MY CLOUD · Upload once, stream on your devices</p></div></header>
<section class="card"><h2>Private access</h2><p>Use the same private access token on your phone and TV browser. Keep it secret; anyone with it can manage your videos.</p>
<label for="token">Cloud access token</label><input id="token" type="password" autocomplete="current-password" placeholder="Paste STREAM_API_TOKEN">
<div class="row"><button id="connect">Connect library</button><button class="secondary" id="forget">Forget token</button></div></section>
<section class="card"><h2>Upload a video</h2><p>The video uploads directly from your device to private cloud storage. It does not need to stay downloaded on the TV.</p>
<label for="file">Choose video file</label><input id="file" type="file" accept="video/*,.mkv,.avi,.ts">
<label for="title">Display title (optional)</label><input id="title" maxlength="150" placeholder="Use the file name if left blank">
<div class="row"><button id="upload">Upload to cloud</button><span id="uploadState" class="small"></span></div>
<progress id="progress" value="0" max="100" class="hidden"></progress></section>
<section class="card"><div class="row"><div><h2 style="margin:0">My videos</h2><p style="margin:6px 0">Choose a title to play it online, or delete it from the cloud.</p></div><button class="secondary" id="refresh">Refresh</button></div>
<div id="videos" class="grid"></div></section>
<section class="card hidden" id="playerCard"><h2 id="nowPlaying">Now playing</h2><video id="player" controls playsinline></video><p class="small">Playback uses your internet connection. MP4 with H.264 video and AAC audio is recommended for broad TV support.</p></section>
<div id="status" role="status" aria-live="polite"></div>
<p class="small">Private personal library. Upload only videos you own or are authorized to store and stream. Cloud storage and internet usage may incur charges.</p>
</main>
<script>
const $=id=>document.getElementById(id);const tokenInput=$("token");tokenInput.value=sessionStorage.getItem("cloudToken")||"";
const auth=()=>({Authorization:"Bearer "+tokenInput.value});
function status(s){$("status").textContent=s}
function bytes(n){if(!n)return "0 B";const u=["B","KB","MB","GB","TB"];let i=0;while(n>=1024&&i<u.length-1){n/=1024;i++}return n.toFixed(i?1:0)+" "+u[i]}
async function api(path,options={}){const r=await fetch(path,{...options,headers:{...auth(),...(options.headers||{})}});let data={};try{data=await r.json()}catch{}if(!r.ok)throw new Error(data.detail||("Request failed ("+r.status+")"));return data}
function safe(s){return String(s).replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]))}
async function refresh(){try{const data=await api("/api/videos");$("videos").innerHTML="";if(!data.videos.length){$("videos").textContent="Your cloud library is empty. Upload a video above.";return}
for(const v of data.videos){const el=document.createElement("article");el.className="card video";el.innerHTML="<strong>"+safe(v.title)+"</strong><p class='size'>"+safe(v.filename)+" · "+bytes(v.sizeBytes)+"</p><button class='play'>▶ Play</button><button class='danger'>Delete</button>";el.querySelector(".play").onclick=()=>play(v);el.querySelector(".danger").onclick=()=>removeVideo(v);$("videos").appendChild(el)}status(data.count+" video(s) in your private cloud.")}catch(e){status(e.message)}}
async function connect(){if(!tokenInput.value.trim()){status("Paste your cloud access token first.");return}sessionStorage.setItem("cloudToken",tokenInput.value.trim());await refresh()}
function play(v){$("playerCard").classList.remove("hidden");$("nowPlaying").textContent="▶ "+v.title;$("player").src=v.playUrl;$("player").load();$("player").play().catch(()=>status("Press Play in the video controls if playback did not start."));$("playerCard").scrollIntoView({behavior:"smooth",block:"start"})}
async function removeVideo(v){if(!confirm("Permanently delete "+v.title+" from cloud storage?"))return;try{await api("/api/videos/"+encodeURIComponent(v.id),{method:"DELETE"});status("Deleted from cloud.");await refresh()}catch(e){status(e.message)}}
async function upload(){const file=$("file").files[0];if(!file){status("Choose a video file first.");return}if(!tokenInput.value.trim()){status("Connect to your cloud library first.");return}
const ext=(file.name.match(/\.[^.]+$/)||[""])[0].toLowerCase();const allowed=[".mp4",".m4v",".mov",".mkv",".webm",".avi",".mpeg",".mpg",".ts"];if(!allowed.includes(ext)){status("This video file type is not supported.");return}
$("upload").disabled=true;$("progress").classList.remove("hidden");$("progress").value=0;
try{const type=file.type&&file.type.startsWith("video/")?file.type:"application/octet-stream";const init=await api("/api/uploads",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({filename:file.name,title:$("title").value||null,contentType:type,sizeBytes:file.size})});
await new Promise((resolve,reject)=>{const x=new XMLHttpRequest();x.open("PUT",init.uploadUrl);x.setRequestHeader("Content-Type",init.contentType);x.upload.onprogress=e=>{if(e.lengthComputable){$("progress").value=Math.round(e.loaded/e.total*100);$("uploadState").textContent=$("progress").value+"%"}};x.onload=()=>x.status>=200&&x.status<300?resolve():reject(new Error("Cloud upload failed ("+x.status+"). Check the bucket CORS setup and try again."));x.onerror=()=>reject(new Error("Upload connection failed. Check internet and bucket CORS."));x.send(file)});
await api("/api/uploads/"+init.videoId+"/complete",{method:"POST"});status("Upload complete. Your video is now in the cloud.");$("file").value="";$("title").value="";await refresh()
}catch(e){status(e.message)}finally{$("upload").disabled=false;$("uploadState").textContent="";}}
$("connect").onclick=connect;$("refresh").onclick=refresh;$("upload").onclick=upload;$("forget").onclick=()=>{sessionStorage.removeItem("cloudToken");tokenInput.value="";$("videos").textContent="Token cleared from this browser session.";status("Token forgotten on this device.")};
</script></body></html>"""


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("stream:app", host="0.0.0.0", port=int(os.getenv("PORT", "8000")))
