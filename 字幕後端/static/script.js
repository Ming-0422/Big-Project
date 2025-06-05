let player;
const subtitleDiv = document.getElementById("subtitle");
let subtitles = []; // 儲存所有字幕物件

// YouTube Iframe API 載入後會呼叫
function onYouTubeIframeAPIReady() {
  // 不先建立 player，等使用者按按鈕時才建立
}

// 解析 YouTube URL，取出影片ID
function getYouTubeID(url) {
  const regExp = /^.*(?:youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
  const match = url.match(regExp);
  return match && match[1].length === 11 ? match[1] : null;
}

// 解析單段 SRT 字幕（時間與文字）
function parseSingleSRT(srtText) {
  const lines = srtText.trim().split("\n");
  if (lines.length < 3) return null;

  const timeMatch = lines[1].match(/(\d{2}:\d{2}:\d{2},\d{3}) --> (\d{2}:\d{2}:\d{2},\d{3})/);
  if (!timeMatch) return null;

  function toSeconds(t) {
    const [h, m, s, ms] = t.split(/[:,]/);
    return (+h) * 3600 + (+m) * 60 + (+s) + (+ms) / 1000;
  }

  return {
    start: toSeconds(timeMatch[1]),
    end: toSeconds(timeMatch[2]),
    text: lines.slice(2).join("\n").trim()
  };
}

// 建立 YouTube 播放器，或直接載入影片並播放
function loadYouTubeVideo(videoId) {
  if (!player) {
    player = new YT.Player("player", {
      height: "360",
      width: "640",
      videoId: videoId,
      events: {
        onReady: () => {
          player.playVideo();
        }
      }
    });
  } else {
    player.loadVideoById(videoId);
    player.playVideo();
  }
}

// 持續更新字幕顯示
function updateSubtitle() {
  if (!player || subtitles.length === 0) {
    requestAnimationFrame(updateSubtitle);
    return;
  }

  const currentTime = player.getCurrentTime();
  let currentSub = null;

  // 從後往前找，找當前時間落在哪個字幕區間
  for (let i = subtitles.length - 1; i >= 0; i--) {
    if (currentTime >= subtitles[i].start && currentTime <= subtitles[i].end) {
      currentSub = subtitles[i];
      break;
    }
  }

  subtitleDiv.textContent = currentSub ? currentSub.text : "";
  requestAnimationFrame(updateSubtitle);
}

// 按鈕事件：開始轉錄並播放影片
document.getElementById("startBtn").onclick = async () => {
  const url = document.getElementById("youtubeUrl").value.trim();
  const videoId = getYouTubeID(url);

  if (!videoId) {
    alert("請輸入正確的 YouTube 連結");
    return;
  }

  subtitleDiv.textContent = "字幕產生中，請稍候...";
  subtitles = []; // 清除之前的字幕

  try {
    const response = await fetch("http://127.0.0.1:8000/api/generate-subtitle", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        youtube_url: url,
        segment_duration: "30"
      })
    });

    if (!response.body) {
      subtitleDiv.textContent = "無法取得字幕串流";
      return;
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    let buffer = "";

    // 讀取串流，一段段處理 SRT
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const parts = buffer.split("\n\n");
      buffer = parts.pop(); // 保留最後不完整片段

      for (const part of parts) {
        const subtitle = parseSingleSRT(part);
        if (subtitle) {
          subtitles.push(subtitle);
        }
      }
    }

    subtitleDiv.textContent = "字幕完成，影片播放中...";
    loadYouTubeVideo(videoId);
  } catch (err) {
    subtitleDiv.textContent = "產生字幕時發生錯誤：" + err.message;
  }
};

// 啟動字幕同步更新迴圈
updateSubtitle();
