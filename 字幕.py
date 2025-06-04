import os
import yt_dlp
import whisper
import srt
import datetime

# 把 ffmpeg 的路徑加入到環境變數 PATH，讓 whisper 調用時能找到
os.environ["PATH"] += r";D:\ffmpeg\ffmpeg-master-latest-win64-gpl\bin"

url = "https://www.youtube.com/watch?v=C4QMPhiF_as"
ydl_opts = {
    'format': 'bestaudio/best',
    'outtmpl': 'downloaded_audio.%(ext)s',
    'postprocessors': [{
        'key': 'FFmpegExtractAudio',
        'preferredcodec': 'mp3',
        'preferredquality': '192',
    }],
    'ffmpeg_location': r'D:\ffmpeg\ffmpeg-master-latest-win64-gpl\bin'  # yt-dlp 也用這個路徑
}

print("開始下載音訊...")
with yt_dlp.YoutubeDL(ydl_opts) as ydl:
    ydl.download([url])
print("下載完成，開始轉錄...")

model = whisper.load_model("base")
result = model.transcribe("downloaded_audio.mp3", verbose=True)

subs = []
for i, segment in enumerate(result["segments"]):
    subs.append(srt.Subtitle(
        index=i + 1,
        start=datetime.timedelta(seconds=segment["start"]),
        end=datetime.timedelta(seconds=segment["end"]),
        content=segment["text"].strip()
    ))

with open("output.srt", "w", encoding="utf-8") as f:
    f.write(srt.compose(subs))

print("字幕已經產生到 output.srt")
