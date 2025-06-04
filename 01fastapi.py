import os
import datetime
import tempfile
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.responses import PlainTextResponse
import whisper
import srt
import yt_dlp

os.environ["PATH"] += r";D:\ffmpeg\ffmpeg-master-latest-win64-gpl\bin"

app = FastAPI()
model = whisper.load_model("base")

@app.post("/api/generate-subtitle", response_class=PlainTextResponse)
async def generate_subtitle(
    file: UploadFile = File(None),
    youtube_url: str = Form(None)
):
    if not file and not youtube_url:
        raise HTTPException(status_code=400, detail="請上傳音訊/影片檔或提供 YouTube 影片 URL")

    try:
        with tempfile.TemporaryDirectory() as tmpdir:
            audio_path = None

            if youtube_url:
                ydl_opts = {
                    'format': 'bestaudio/best',
                    'outtmpl': os.path.join(tmpdir, 'downloaded_audio.%(ext)s'),
                    'postprocessors': [{
                        'key': 'FFmpegExtractAudio',
                        'preferredcodec': 'mp3',
                        'preferredquality': '192',
                    }],
                    'ffmpeg_location': r'D:\ffmpeg\ffmpeg-master-latest-win64-gpl\bin'
                }
                with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                    ydl.download([youtube_url])

                for f in os.listdir(tmpdir):
                    if f.endswith(".mp3"):
                        audio_path = os.path.join(tmpdir, f)
                        break
                if audio_path is None:
                    raise HTTPException(status_code=500, detail="下載音訊失敗")
            else:
                audio_path = os.path.join(tmpdir, file.filename)
                with open(audio_path, "wb") as f:
                    f.write(await file.read())

            result = model.transcribe(audio_path, verbose=False)

            subs = []
            for i, segment in enumerate(result["segments"]):
                subs.append(srt.Subtitle(
                    index=i + 1,
                    start=datetime.timedelta(seconds=segment["start"]),
                    end=datetime.timedelta(seconds=segment["end"]),
                    content=segment["text"].strip()
                ))

            subtitle_text = srt.compose(subs)
            return subtitle_text
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"轉錄失敗: {e}")
