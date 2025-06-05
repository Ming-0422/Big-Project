from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.responses import StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

import os
import tempfile
import datetime
import subprocess
from pathlib import Path
from typing import AsyncGenerator

import srt
import yt_dlp
import whisper

# 設定 ffmpeg 路徑
FFMPEG_PATH = r"D:\ffmpeg\ffmpeg-master-latest-win64-gpl\bin"

app = FastAPI()

# 允許前端跨域
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 靜態檔案（可用於前端 HTML 等）
app.mount("/static", StaticFiles(directory="static"), name="static")

# 載入 Whisper 模型
try:
    model = whisper.load_model("base")
except Exception as e:
    raise RuntimeError(f"模型載入失敗: {e}")

# 取得音訊長度（秒）
def get_audio_duration(audio_path: str) -> float:
    result = subprocess.run([
        os.path.join(FFMPEG_PATH, "ffprobe"),
        "-v", "error",
        "-show_entries", "format=duration",
        "-of", "default=noprint_wrappers=1:nokey=1",
        audio_path
    ], stdout=subprocess.PIPE)
    return float(result.stdout.decode().strip())

# 分割音訊
def split_audio(audio_path, segment_duration=30):
    output_dir = Path(audio_path).parent / "segments"
    output_dir.mkdir(exist_ok=True)

    total_duration = get_audio_duration(audio_path)
    segments = []

    for start_time in range(0, int(total_duration), segment_duration):
        segment_path = output_dir / f"segment_{start_time}.mp3"
        cmd = [
            os.path.join(FFMPEG_PATH, "ffmpeg"),
            "-i", audio_path,
            "-ss", str(start_time),
            "-t", str(segment_duration),
            "-acodec", "libmp3lame",
            "-q:a", "2",
            "-ar", "44100",
            str(segment_path),
            "-y"
        ]
        subprocess.run(cmd, check=True, capture_output=True)
        segments.append((start_time, str(segment_path)))

    return segments

# 逐段處理音訊並串流字幕
async def process_audio_segment_stream(segment_path: str, start_time: float) -> AsyncGenerator[str, None]:
    result = model.transcribe(segment_path, verbose=False)
    for i, seg in enumerate(result["segments"]):
        seg["start"] += start_time
        seg["end"] += start_time
        subtitle = srt.Subtitle(
            index=i+1,
            start=datetime.timedelta(seconds=seg["start"]),
            end=datetime.timedelta(seconds=seg["end"]),
            content=seg["text"].strip()
        )
        yield srt.compose([subtitle]) + "\n"

# API：產生字幕串流
@app.post("/api/generate-subtitle")
async def generate_subtitle(
    file: UploadFile = File(None),
    youtube_url: str = Form(None),
    segment_duration: int = Form(30)
):
    if not file and not youtube_url:
        raise HTTPException(status_code=400, detail="請上傳音訊檔或提供 YouTube 連結")

    async def generate():
        try:
            with tempfile.TemporaryDirectory() as tmpdir:
                audio_path = None

                # 下載 YouTube 音訊
                if youtube_url:
                    ydl_opts = {
                        'format': 'bestaudio/best',
                        'outtmpl': os.path.join(tmpdir, 'audio.%(ext)s'),
                        'postprocessors': [{
                            'key': 'FFmpegExtractAudio',
                            'preferredcodec': 'mp3',
                            'preferredquality': '192',
                        }],
                        'ffmpeg_location': FFMPEG_PATH
                    }
                    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                        ydl.download([youtube_url])
                    for f in os.listdir(tmpdir):
                        if f.endswith(".mp3"):
                            audio_path = os.path.join(tmpdir, f)
                            break

                # 處理上傳音訊
                elif file:
                    audio_path = os.path.join(tmpdir, file.filename)
                    with open(audio_path, "wb") as f_out:
                        f_out.write(await file.read())

                if not audio_path:
                    raise HTTPException(status_code=500, detail="音訊檔案處理失敗")

                # 分段 + 逐段轉錄
                segments = split_audio(audio_path, segment_duration)
                for start_time, segment_path in segments:
                    async for subtitle in process_audio_segment_stream(segment_path, start_time):
                        yield subtitle

        except Exception as e:
            raise HTTPException(status_code=500, detail=f"字幕產生失敗: {str(e)}")

    return StreamingResponse(generate(), media_type="text/plain")
