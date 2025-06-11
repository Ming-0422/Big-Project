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
import asyncio
from concurrent.futures import ThreadPoolExecutor
import torch
import shutil
import translators as ts

import srt
import yt_dlp
import whisper

# 設定 ffmpeg 路徑
def find_ffmpeg_path():
    """
    自動尋找 FFMPEG 路徑，提升可攜性。
    優先順序:
    1. 系統 PATH
    2. FFMPEG_PATH 環境變數
    3. (備用) 原始的硬編碼路徑 (僅限 Windows)
    """
    # 1. 優先檢查系統 PATH
    ffmpeg_exec = shutil.which("ffmpeg")
    if ffmpeg_exec:
        path = os.path.dirname(ffmpeg_exec)
        print(f"在系統 PATH 中找到 ffmpeg: {path}")
        return path

    # 2. 檢查 FFMPEG_PATH 環境變數
    if "FFMPEG_PATH" in os.environ:
        path = os.environ["FFMPEG_PATH"]
        # 簡單驗證路徑下是否有 ffmpeg 執行檔
        if os.path.exists(os.path.join(path, "ffmpeg")) or os.path.exists(os.path.join(path, "ffmpeg.exe")):
            print(f"在 FFMPEG_PATH 環境變數中找到 ffmpeg: {path}")
            return path

    # 3. 備用的硬編碼路徑 (帶有警告)
    win_path = r"D:\ffmpeg\ffmpeg-master-latest-win64-gpl\bin"
    if os.name == 'nt' and os.path.exists(os.path.join(win_path, "ffmpeg.exe")):
        print(f"警告：使用硬編碼的備用路徑。建議將 ffmpeg 加入系統 PATH 或設定 FFMPEG_PATH 環境變數。路徑: {win_path}")
        return win_path

    raise RuntimeError(
        "找不到 ffmpeg。請將 ffmpeg 執行檔所在的資料夾加入系統 PATH，"
        "或設定一個名為 FFMPEG_PATH 的環境變數指向該資料夾。"
    )

FFMPEG_PATH = find_ffmpeg_path()

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

# 建立線程池
executor = ThreadPoolExecutor(max_workers=4)

# 檢查 CUDA 是否可用
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
print(f"使用設備: {DEVICE}")
if DEVICE == "cuda":
    print(f"GPU型號: {torch.cuda.get_device_name(0)}")

# 載入 Whisper 模型
try:
    model = whisper.load_model("base").to(DEVICE)
    print("模型載入完成")
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
def split_audio(audio_path, segment_duration=15):  # 縮短片段長度
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
            "-q:a", "4",  # 降低音質以加快處理
            "-ar", "16000",  # 降低採樣率
            str(segment_path),
            "-y"
        ]
        subprocess.run(cmd, check=True, capture_output=True)
        segments.append((start_time, str(segment_path)))

    return segments

# 時間格式化函數
def format_time(seconds):
    """將秒數轉換為 SRT 格式的時間字串 (HH:MM:SS,mmm)"""
    hours = int(seconds // 3600)
    minutes = int((seconds % 3600) // 60)
    seconds = seconds % 60
    milliseconds = int((seconds % 1) * 1000)
    seconds = int(seconds)
    return f"{hours:02d}:{minutes:02d}:{seconds:02d},{milliseconds:03d}"

# 處理音訊片段
def process_audio_segment(segment_path: str, start_time: float = 0, language: str = None) -> list:
    try:
        print(f"開始處理音訊片段：{segment_path}，語言：{language or '自動偵測'}")
        
        transcribe_options = {
            "task": "transcribe",
            "verbose": False,
            "fp16": torch.cuda.is_available()
        }
        if language and language != "auto":
            transcribe_options["language"] = language

        result = model.transcribe(
            segment_path,
            **transcribe_options
        )
        
        # 返回結構化的片段列表，而不是 SRT 字串
        processed_segments = []
        for segment in result["segments"]:
            processed_segments.append({
                "start": segment["start"] + start_time,
                "end": segment["end"] + start_time,
                "text": segment['text'].strip()
            })
            
        print(f"音訊片段處理完成，產生了 {len(processed_segments)} 個字幕片段")
        return processed_segments
        
    except Exception as e:
        print(f"處理音訊片段時發生錯誤: {e}")
        raise

# API：產生字幕串流
@app.post("/api/generate-subtitle")
async def generate_subtitle(
    file: UploadFile = File(None),
    youtube_url: str = Form(None),
    segment_duration: int = Form(15),
    source_language: str = Form("auto"),
    target_language: str = Form(None)
):
    if not file and not youtube_url:
        raise HTTPException(status_code=400, detail="請上傳音訊檔或提供 YouTube 連結")

    tmpdir = tempfile.TemporaryDirectory()
    loop = asyncio.get_event_loop()

    try:
        # --- 1. 準備階段 (在線程中執行，避免阻塞) ---
        def prepare_audio(url, seg_duration, temp_dir_name, file_content=None, filename=None):
            audio_path = None
            if url:
                print("開始下載 YouTube 音訊...")
                ydl_opts = {
                    'format': 'bestaudio/best',
                    'outtmpl': os.path.join(temp_dir_name, 'audio.%(ext)s'),
                    'postprocessors': [{
                        'key': 'FFmpegExtractAudio',
                        'preferredcodec': 'mp3',
                        'preferredquality': '128',
                    }],
                    'ffmpeg_location': FFMPEG_PATH,
                    'noplaylist': True
                }
                with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                    ydl.download([url])
                for f in os.listdir(temp_dir_name):
                    if f.endswith(".mp3"):
                        audio_path = os.path.join(temp_dir_name, f)
                        break
            elif file_content:
                print("正在處理上傳的音訊檔...")
                audio_path = os.path.join(temp_dir_name, filename)
                with open(audio_path, "wb") as f_out:
                    f_out.write(file_content)

            if not audio_path:
                raise ValueError("音訊檔案處理失敗，找不到有效的音訊來源。")
            
            print("音訊準備完成，開始分割...")
            segments = split_audio(audio_path, seg_duration)
            print(f"音訊分割完成，共 {len(segments)} 個片段。")
            return segments

        file_content = await file.read() if file else None
        filename = file.filename if file else None
        segments = await loop.run_in_executor(
            executor, 
            prepare_audio, 
            youtube_url, 
            segment_duration,
            tmpdir.name,
            file_content,
            filename
        )

    except Exception as e:
        tmpdir.cleanup()
        print(f"字幕前置處理失敗: {e}")
        raise HTTPException(status_code=500, detail=f"字幕前置處理失敗: {str(e)}")

    # --- 2. 串流階段 ---
    async def stream_generator():
        try:
            print(f"前置作業完成，開始串流字幕... 目標語言: {target_language or '無'}")
            srt_id_counter = 1
            for start_time, segment_path in segments:
                # 獲取轉錄後的結構化片段
                transcribed_segments = await loop.run_in_executor(
                    executor, process_audio_segment, segment_path, start_time, source_language
                )
                
                # 如果指定了目標語言，則在串流前進行翻譯
                if target_language and target_language != "none" and transcribed_segments:
                    texts_to_translate = [seg['text'] for seg in transcribed_segments]
                    
                    try:
                        # 更換為 Bing 翻譯服務，提高穩定性
                        translated_texts = await loop.run_in_executor(
                            executor,
                            lambda: [ts.translate_text(text, translator='bing', to_language=target_language) for text in texts_to_translate]
                        )
                        # 將翻譯後的文字寫回
                        for i, seg in enumerate(transcribed_segments):
                            seg['text'] = translated_texts[i]
                    except Exception as trans_err:
                        print(f"翻譯時發生錯誤，將返回原文: {trans_err}")

                # 將處理完的片段（原文或譯文）格式化為 SRT 字串並傳送
                srt_content_chunk = ""
                for seg in transcribed_segments:
                    start_time_str = format_time(seg['start'])
                    end_time_str = format_time(seg['end'])
                    
                    srt_content_chunk += f"{srt_id_counter}\n"
                    srt_content_chunk += f"{start_time_str} --> {end_time_str}\n"
                    srt_content_chunk += f"{seg['text']}\n\n"
                    
                    srt_id_counter += 1
                
                if srt_content_chunk:
                    yield srt_content_chunk

            print("字幕串流結束。")
        except Exception as e:
            print(f"串流期間發生錯誤: {e}")
        finally:
            tmpdir.cleanup()
            print("暫存資料夾已清除。")

    return StreamingResponse(stream_generator(), media_type="text/plain")

# API：翻譯文字 (此 API 在新流程中非必要，但予以保留)
@app.post("/api/translate")
async def translate_text_api(request: dict):
    texts = request.get("texts", [])
    target_language = request.get("target_language", "en")

    if not texts:
        raise HTTPException(status_code=400, detail="沒有提供需要翻譯的文字")

    try:
        # 使用線程池執行翻譯，避免阻塞
        loop = asyncio.get_event_loop()
        translated_texts = await loop.run_in_executor(
            executor,
            lambda: [ts.translate_text(text, to_language=target_language) for text in texts]
        )
        return {"translated_texts": translated_texts}
    except Exception as e:
        print(f"翻譯時發生錯誤: {e}")
        raise HTTPException(status_code=500, detail=f"翻譯失敗: {str(e)}")