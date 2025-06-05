from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.responses import PlainTextResponse
from fastapi.middleware.cors import CORSMiddleware
import os
import tempfile
import datetime
import srt
import yt_dlp
import whisper

# 初始化 FastAPI 應用
app = FastAPI()

# 加入 CORS 中介軟體，允許跨來源請求（方便前端存取）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 載入 Whisper 模型（可替換為 base/small/medium/large）
try:
    model = whisper.load_model("medium")
except Exception as e:
    raise RuntimeError(f"模型載入失敗: {e}")

# 產生字幕的 API 端點
@app.post("/api/generate-subtitle")
async def generate_subtitle(
    file: UploadFile = File(None),             # 上傳的音訊/影片檔案
    youtube_url: str = Form(None)             # YouTube 網址
):
    # 若未提供任何輸入
    if not file and not youtube_url:
        raise HTTPException(status_code=400, detail="請上傳音訊檔或提供 YouTube 連結")

    try:
        # 建立臨時資料夾存放檔案
        with tempfile.TemporaryDirectory() as tmpdir:
            audio_path = None  # 儲存音訊檔路徑

            # 🎥 若使用 YouTube URL 下載音訊
            if youtube_url:
                ydl_opts = {
                    'format': 'bestaudio/best',
                    'outtmpl': os.path.join(tmpdir, 'audio.%(ext)s'),
                    'postprocessors': [{
                        'key': 'FFmpegExtractAudio',
                        'preferredcodec': 'mp3',
                        'preferredquality': '192',
                    }],
                    'ffmpeg_location': r'D:\ffmpeg\ffmpeg-master-latest-win64-gpl\bin'  # ⚠️ 請修改為你本機的 ffmpeg 路徑
                }

                # 下載音訊
                with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                    ydl.download([youtube_url])

                # 找出下載後的音訊檔案
                for f in os.listdir(tmpdir):
                    if f.endswith(".mp3"):
                        audio_path = os.path.join(tmpdir, f)
                        break

                if not audio_path:
                    raise HTTPException(status_code=500, detail="YouTube 音訊下載失敗")

            # 🎵 若上傳本地音訊檔案
            elif file:
                audio_path = os.path.join(tmpdir, file.filename)
                with open(audio_path, "wb") as f_out:
                    f_out.write(await file.read())

            # 🧠 使用 Whisper 模型進行轉錄
            result = model.transcribe(audio_path, verbose=False)

            # 🧾 轉換為 SRT 字幕物件
            subtitles = [
                srt.Subtitle(
                    index=i + 1,
                    start=datetime.timedelta(seconds=seg["start"]),
                    end=datetime.timedelta(seconds=seg["end"]),
                    content=seg["text"].strip()
                )
                for i, seg in enumerate(result["segments"])
            ]

            # 產生 SRT 字幕文字
            srt_text = srt.compose(subtitles)

            # 💾 將 SRT 字幕儲存為檔案（output.srt）
            output_path = os.path.join(tmpdir, "output.srt")
            with open(output_path, "w", encoding="utf-8") as f:
                f.write(srt_text)

            # ✅ 如果想保存至永久資料夾可改用 shutil.move() 將檔案移出 tmpdir

            # 📤 回傳純文字字幕內容給前端（不包含檔案下載）
            return PlainTextResponse(srt_text, media_type="text/plain")

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"字幕產生失敗: {e}")
