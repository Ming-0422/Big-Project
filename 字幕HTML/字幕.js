(() => {
  // ===== 全域變數 =====
  const form = document.getElementById("uploadForm");
  const fileInput = document.getElementById("fileInput");
  const youtubeInput = document.getElementById("youtubeInput");
  const youtubeSubtitle = document.getElementById("youtubeSubtitle");
  const youtubeIframe = document.getElementById("youtubeIframe");

  let subtitles = [];       // 解析後的字幕陣列 [{start, end, text}]
  let player = null;        // YouTube 播放器物件
  let subtitleInterval = null;
  let lastSubtitle = "";    // 用來避免字幕閃爍

  // ===== 解析 SRT 字幕文字，回傳字幕物件陣列 =====
  function parseSRT(srtText) {
    const srtRegex = /(\d+)\s+(\d{2}:\d{2}:\d{2},\d{3}) --> (\d{2}:\d{2}:\d{2},\d{3})\s+([\s\S]*?)(?=\n\n|\n*$)/g;
    const toSeconds = timeStr => {
      const [h, m, sms] = timeStr.split(':');
      const [s, ms] = sms.split(',');
      return Number(h) * 3600 + Number(m) * 60 + Number(s) + Number(ms) / 1000;
    };

    let result = [];
    let match;
    while ((match = srtRegex.exec(srtText)) !== null) {
      result.push({
        start: toSeconds(match[2]),
        end: toSeconds(match[3]),
        text: match[4].trim()
      });
    }
    return result;
  }

  // ===== YouTube IFrame API 初始化 =====
  window.onYouTubeIframeAPIReady = function () {
    if (player) {
      player.destroy(); // 移除舊播放器避免衝突
      player = null;
    }
    player = new YT.Player('youtubeIframe', {
      events: {
        onReady: onPlayerReady,
        onStateChange: onPlayerStateChange
      }
    });
  };

  function onPlayerReady(event) {
    // 可以放自動播放或其他設定
    // event.target.playVideo();
  }

  // 播放狀態變化時控制字幕更新
  function onPlayerStateChange(event) {
    if (event.data === YT.PlayerState.PLAYING) {
      subtitleInterval = setInterval(updateSubtitle, 300);
    } else {
      clearInterval(subtitleInterval);
      youtubeSubtitle.innerText = "";
      lastSubtitle = "";
    }
  }

  // 更新字幕文字顯示
  function updateSubtitle() {
    if (!player || subtitles.length === 0) return;

    const currentTime = player.getCurrentTime();
    const currentSub = subtitles.find(sub => currentTime >= sub.start && currentTime <= sub.end);

    if (currentSub && currentSub.text !== lastSubtitle) {
      youtubeSubtitle.innerText = currentSub.text;
      lastSubtitle = currentSub.text;
    } else if (!currentSub && lastSubtitle !== "") {
      youtubeSubtitle.innerText = "";
      lastSubtitle = "";
    }
  }

  // ===== 擷取 YouTube 影片 ID =====
  function extractYouTubeVideoId(url) {
    const regex = /(?:youtube\.com.*(?:\?v=|\/embed\/|\/v\/|\/watch\?v=)|youtu\.be\/)([^&?/]+)/;
    const match = url.match(regex);
    return match ? match[1] : null;
  }

  // ===== 表單送出事件 =====
  form.addEventListener("submit", async event => {
    event.preventDefault();

    subtitles = [];
    lastSubtitle = "";
    youtubeSubtitle.innerText = "";

    const formData = new FormData();
    const hasFile = fileInput.files.length > 0;
    const youtubeUrl = youtubeInput.value.trim();

    if (!hasFile && youtubeUrl === "") {
      alert("請選擇檔案或輸入 YouTube 連結");
      return;
    }

    if (hasFile) formData.append("file", fileInput.files[0]);
    if (youtubeUrl !== "") {
      formData.append("youtube_url", youtubeUrl);

      const videoId = extractYouTubeVideoId(youtubeUrl);
      if (videoId) {
        // 設定 iframe src，enablejsapi=1 啟用 JS API
        youtubeIframe.src = `https://www.youtube.com/embed/${videoId}?enablejsapi=1`;
      }
    }

    try {
      // 呼叫後端 API 取得 SRT 字幕
      const response = await fetch("http://127.0.0.1:8000/api/generate-subtitle", {
        method: "POST",
        body: formData
      });

      if (!response.ok) {
        const err = await response.text();
        alert("轉字幕失敗：" + err);
        return;
      }

      const srtText = await response.text();

      // 解析字幕文字
      subtitles = parseSRT(srtText);

      // 載入 YouTube IFrame API（如果尚未載入）
      if (!window.YT || !window.YT.Player) {
        const tag = document.createElement("script");
        tag.src = "https://www.youtube.com/iframe_api";
        document.head.appendChild(tag);
      } else {
        onYouTubeIframeAPIReady();
      }
    } catch (err) {
      alert("錯誤：" + err.message);
    }
  });
})();
