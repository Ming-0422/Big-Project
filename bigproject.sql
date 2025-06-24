-- phpMyAdmin SQL Dump
-- version 5.1.2
-- https://www.phpmyadmin.net/
--
-- 主機： localhost:3306
-- 產生時間： 2025-06-24 03:02:57
-- 伺服器版本： 5.7.24
-- PHP 版本： 8.3.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 資料庫: `bigproject`
--

-- --------------------------------------------------------

--
-- 資料表結構 `answer`
--

CREATE TABLE `answer` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `quiz_id` bigint(20) DEFAULT NULL,
  `selected_option` int(11) DEFAULT NULL,
  `is_correct` tinyint(1) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `answered_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `source` varchar(10) DEFAULT 'local',
  `video_id` bigint(20) DEFAULT NULL,
  `attempt_id` bigint(20) NOT NULL,
  `submitted_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 傾印資料表的資料 `answer`
--

INSERT INTO `answer` (`id`, `user_id`, `quiz_id`, `selected_option`, `is_correct`, `created_at`, `answered_at`, `source`, `video_id`, `attempt_id`, `submitted_at`) VALUES
(496, 123, 767, 0, 0, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL),
(497, 123, 768, 1, 0, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL),
(498, 123, 769, 2, 0, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL),
(499, 123, 770, 1, 1, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL),
(500, 123, 771, 0, 0, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL),
(501, 123, 782, 0, 0, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL),
(502, 123, 783, 1, 0, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL),
(503, 123, 784, 2, 1, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL),
(504, 123, 785, 1, 0, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL),
(505, 123, 786, 0, 0, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL);

-- --------------------------------------------------------

--
-- 資料表結構 `chat_messages`
--

CREATE TABLE `chat_messages` (
  `id` bigint(20) NOT NULL,
  `message` text NOT NULL,
  `role` enum('ASSISTANT','USER') NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `member_id` bigint(20) NOT NULL,
  `session_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 傾印資料表的資料 `chat_messages`
--

INSERT INTO `chat_messages` (`id`, `message`, `role`, `timestamp`, `member_id`, `session_id`) VALUES
(13, '什麼是HTML?', 'USER', '2025-06-23 14:56:37.633204', 1, 7),
(14, 'HTML是超文本標記語言（HyperText Markup Language）的縮寫。它是一種用來創建和設計網頁的標記語言。HTML使用標籤來結構化網頁內容，如文本、圖像、鏈接和其他媒體元素。標籤通常成對使用，包含開始和結束標籤，用來包圍網頁元素內容。HTML是建立網頁和網站的基礎技術之一，與CSS和JavaScript一起，讓網頁變得豐富和有互動性。', 'ASSISTANT', '2025-06-23 14:56:40.393045', 1, 7),
(15, '什麼是CSS?', 'USER', '2025-06-23 14:56:55.316895', 1, 8),
(16, 'CSS是指「層疊樣式表」（Cascading Style Sheets），它是一種用來控制網頁外觀和佈局的語言。透過CSS，你可以設定HTML元素的樣式，包括顏色、字體、間距、對齊、邊框等。這樣可以讓網頁看起來更美觀和一致，更容易維護。CSS使設計者能夠分離內容（HTML）和樣式（CSS），提高網頁設計和開發的效率。', 'ASSISTANT', '2025-06-23 14:56:57.847009', 1, 8),
(17, '什麼是Java Script?', 'USER', '2025-06-23 14:57:37.196017', 1, 9),
(18, 'JavaScript是一種高級的、解釋型的程式語言，主要用於增加網頁的互動性。它是客戶端腳本語言，被廣泛應用於網頁開發中。JavaScript可以控制瀏覽器的動作、更新網頁內容、驗證表單數據，以及建構各種動態的使用者體驗。作為前端三大技術之一（另外兩個是HTML和CSS），JavaScript在現代網路應用中扮演著重要的角色。', 'ASSISTANT', '2025-06-23 14:57:41.572000', 1, 9);

-- --------------------------------------------------------

--
-- 資料表結構 `chat_sessions`
--

CREATE TABLE `chat_sessions` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `member_id` bigint(20) NOT NULL,
  `video_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 傾印資料表的資料 `chat_sessions`
--

INSERT INTO `chat_sessions` (`id`, `created_at`, `title`, `member_id`, `video_id`) VALUES
(7, '2025-06-23 14:55:53.152971', 'HTML簡介與功能解析', 1, NULL),
(8, '2025-06-23 14:56:48.340260', 'CSS是什麼？簡單介紹與用途', 1, NULL),
(9, '2025-06-23 14:57:01.884049', 'JavaScript的定義和作用介紹', 1, NULL);

-- --------------------------------------------------------

--
-- 資料表結構 `members`
--

CREATE TABLE `members` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 NOT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 NOT NULL,
  `coin` bigint(20) NOT NULL DEFAULT '0',
  `current_theme` varchar(255) CHARACTER SET utf8mb4 NOT NULL DEFAULT 'default'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 傾印資料表的資料 `members`
--

INSERT INTO `members` (`id`, `email`, `password`, `username`, `coin`, `current_theme`) VALUES
(1, 'test@example.com', '123456', 'test', 0, 'default');

-- --------------------------------------------------------

--
-- 資料表結構 `quiz`
--

CREATE TABLE `quiz` (
  `quiz_id` bigint(20) NOT NULL,
  `video_id` bigint(20) DEFAULT NULL,
  `question` text,
  `option1` text,
  `option2` text,
  `option3` text,
  `option4` text,
  `correct_index` int(11) DEFAULT NULL,
  `explanation` text,
  `difficulty` varchar(10) DEFAULT NULL,
  `source` varchar(10) DEFAULT 'local',
  `type` enum('single','multi','text') DEFAULT 'single'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 傾印資料表的資料 `quiz`
--

INSERT INTO `quiz` (`quiz_id`, `video_id`, `question`, `option1`, `option2`, `option3`, `option4`, `correct_index`, `explanation`, `difficulty`, `source`, `type`) VALUES
(767, 1, 'CSS 的主要用途是什麼？', '結構化資料', '改變網站外觀', '傳送表單', '儲存資料', 1, 'CSS 是用來改變網頁的外觀', 'easy', 'local', 'single'),
(768, 1, 'CSS 的全名是什麼？', 'Cat Style Sheet', 'Cascading Style Suit', 'Cascading Style Sheets', 'Computer Style Standard', 2, '正確全名為 Cascading Style Sheets', 'easy', 'local', 'single'),
(769, 1, '哪個程式碼編輯器在影片中推薦使用？', 'Notepad++', 'VS Code', 'Atom', 'Word', 1, '影片中推薦使用 VS Code', 'easy', 'local', 'single'),
(770, 1, '撰寫 CSS 最少需要具備什麼知識？', '資料庫設計', 'HTML 基礎', 'JavaScript', 'React.js', 1, '需具備 HTML 基礎知識', 'easy', 'local', 'single'),
(771, 1, 'CSS 搭配哪種語言一起運作？', 'Python', 'C++', 'HTML', 'Java', 2, 'CSS 是搭配 HTML 使用', 'easy', 'local', 'single'),
(772, 1, '使用瀏覽器的主要原因為何？', '顯示圖片', '測試資料庫', '執行 HTML/CSS 程式碼', '傳送 HTTP 請求', 2, '瀏覽器可直接呈現 HTML/CSS 程式碼', 'medium', 'local', 'single'),
(773, 1, 'CSS 是用來「美化網站」，這表示它主要負責？', '網頁排程', '伺服器設定', '網頁外觀', '資料儲存', 2, 'CSS 控制的是外觀樣式', 'medium', 'local', 'single'),
(774, 1, '影片中提到可用的瀏覽器不包含哪一個？', 'Chrome', 'Firefox', 'Safari', 'Edge', 2, '影片中未提及 Safari', 'medium', 'local', 'single'),
(775, 1, 'Visual Studio Code 的簡稱是？', 'VSC', 'VC', 'VS Code', 'VSCodex', 2, '影片中明確提到 VS Code', 'medium', 'local', 'single'),
(776, 1, '為何不能只用 HTML？', 'HTML 太簡單', 'HTML 無法呈現邏輯', 'HTML 不支援動畫', 'HTML 沒有樣式', 3, 'HTML 本身沒有外觀設計', 'medium', 'local', 'single'),
(777, 1, '影片中建議使用哪個瀏覽器？', 'Safari', 'Edge', 'Chrome', 'Opera', 2, '建議使用 Chrome', 'hard', 'local', 'single'),
(778, 1, 'CSS 的功能為何重要？', '改善效能', '改善設計', '改善程式邏輯', '改善資料傳輸', 1, 'CSS 主要是視覺與設計美化', 'hard', 'local', 'single'),
(779, 1, '若網站只有 HTML，會有什麼問題？', '速度慢', '沒功能', '無法美觀', '不支援互動', 2, '只有 HTML 沒有外觀設計', 'hard', 'local', 'single'),
(780, 1, 'CSS 名稱為何容易混淆？', '與 JS 相似', '英文難懂', '翻譯困難', '聽起來複雜', 3, '影片中說名稱聽起來複雜', 'hard', 'local', 'single'),
(781, 1, '影片是否建議沒有 HTML 基礎就開始學 CSS？', '是', '否', '不確定', '沒提到', 1, '影片建議具備 HTML 基礎', 'hard', 'local', 'single'),
(782, 2, '建立按鈕最基本的 HTML 元素是？', '<input>', '<button>', '<div>', '<form>', 1, '<button> 是 HTML 按鈕元素', 'easy', 'local', 'single'),
(783, 2, '改變按鈕背景顏色使用的 CSS 屬性是？', 'color', 'font-size', 'background', 'width', 2, '使用 background 可改變背景', 'easy', 'local', 'single'),
(784, 2, '要讓文字變白使用哪個屬性？', 'text-color', 'font', 'color', 'white-space', 2, 'color 控制文字顏色', 'easy', 'local', 'single'),
(785, 2, '影片中將按鈕邊角變圓的方法是？', 'padding', 'margin', 'border-radius', 'shape', 2, '使用 border-radius 調整圓角', 'easy', 'local', 'single'),
(786, 2, 'border-radius 設成 60px 的效果是？', '變透明', '完全圓形', '邊框變粗', '文字消失', 1, '邊角變得更圓潤', 'easy', 'local', 'single'),
(787, 2, '若要製作有連結功能的按鈕，應包在哪個元素內？', '<div>', '<a>', '<form>', '<span>', 1, '影片中示範將按鈕放在 <a> 中', 'medium', 'local', 'single'),
(788, 2, '影片中將按鈕導向哪個網站？', 'Yahoo', 'Facebook', 'Google', 'YouTube', 2, '連結設定為 Google', 'medium', 'local', 'single'),
(789, 2, '為了讓按鈕樣式美化，需搭配？', 'HTML 註解', 'CSS', 'PHP', 'SQL', 1, '需搭配 CSS', 'medium', 'local', 'single'),
(790, 2, '讓按鈕變圓最常使用的屬性是？', 'outline', 'radius', 'border', 'border-radius', 3, 'border-radius 為標準屬性', 'medium', 'local', 'single'),
(791, 2, '影片中按鈕改變樣式的動作是？', '拖曳', '按鍵', '儲存', '重啟', 2, '修改後儲存產生效果', 'medium', 'local', 'single'),
(792, 2, '影片中將 border-radius 從多少改成 60？', '10', '20', '30', '5', 3, '影片先設 5 再改成 60', 'hard', 'local', 'single'),
(793, 2, '影片提到 border-radius 詳細講解會放在？', '結尾', '下一集', '之後章節', '主題外', 2, '會在之後章節詳細說明', 'hard', 'local', 'single'),
(794, 2, '按鈕修改樣式使用的是哪種語言？', 'JS', 'PHP', 'HTML', 'CSS', 3, 'CSS 負責樣式', 'hard', 'local', 'single'),
(795, 2, '影片主題為？', 'CSS 過渡動畫', '按鈕樣式', 'JS 控制按鈕', 'SQL 撰寫', 1, '重點為按鈕與 CSS', 'hard', 'local', 'single'),
(796, 2, '影片中按鈕加圓角是為了？', '更小體積', '視覺和諧', '滑鼠互動', '支援手機', 1, '讓按鈕不尖銳', 'hard', 'local', 'single'),
(797, 3, '影片推薦使用哪個線上編輯器？', 'JSBin', 'CodePen', 'Grich.com', 'Replit', 2, '推薦使用 Grich.com', 'easy', 'local', 'single'),
(798, 3, 'Grich.com 的用途是？', '儲存影片', '編輯專案', '測試 API', '製作影片', 1, '用來撰寫與管理專案', 'easy', 'local', 'single'),
(799, 3, '影片中開新專案要點哪個按鈕？', 'New Code', 'New Page', 'New Project', 'New Folder', 2, 'New Project 為建新專案', 'easy', 'local', 'single'),
(800, 3, '影片中說 Grich 比較適合什麼？', '大型專案', '視覺動畫', '小實驗', '部署伺服器', 2, '適合小專案', 'easy', 'local', 'single'),
(801, 3, '影片中用什麼檔名來寫 JS？', 'main.js', 'app.js', 'todo.js', 'v3.js', 3, '在 v3.js 測試 console', 'easy', 'local', 'single'),
(802, 3, '影片使用的測試瀏覽器是？', 'Firefox', 'Chrome', 'Edge', 'Safari', 1, '推薦用 Chrome', 'medium', 'local', 'single'),
(803, 3, '影片中 console 測試內容是？', 'hello world', 'V3 ready', 'Hello from V3', 'Bye V3', 2, '示範 Hello from V3', 'medium', 'local', 'single'),
(804, 3, '影片中使用 HTML 檔案名稱為？', 'index.html', 'main.html', 'base.html', 'page.html', 0, 'index.html 為入口頁', 'medium', 'local', 'single'),
(805, 3, '影片在開專案後先做什麼測試？', 'CSS 測試', 'console.log', 'alert', 'AJAX', 1, '打 console 測試連結', 'medium', 'local', 'single'),
(806, 3, '影片提到 Grich 與哪個編輯器相似？', 'VS Code', 'Copen', 'Sublime', 'Brackets', 1, '與 Copen 相似', 'medium', 'local', 'single'),
(807, 3, '影片中 console.log 是在哪個檔案執行？', 'index.html', 'v3.js', 'style.css', 'main.js', 1, 'console 放在 v3.js', 'hard', 'local', 'single'),
(808, 3, '影片提到切換 script 為哪個版本？', 'v1', 'v2', 'v3', 'main', 2, '切換為 v3.js', 'hard', 'local', 'single'),
(809, 3, '影片中提到自動更新功能是哪個工具？', 'VS Code', 'Grich', 'GitHub', 'Notepad++', 1, 'Grich 有自動更新', 'hard', 'local', 'single'),
(810, 3, '影片提到 script.js 的狀況？', '會用', '不會用', '已刪除', '改名', 1, 'script.js 不會用到', 'hard', 'local', 'single'),
(811, 3, '影片結尾說什麼？', '結束', '下次見', '完成', '未完成', 1, '影片以下次見結尾', 'hard', 'local', 'single');

-- --------------------------------------------------------

--
-- 資料表結構 `quiz_results`
--

CREATE TABLE `quiz_results` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `video_id` bigint(20) NOT NULL,
  `total_questions` int(11) DEFAULT NULL,
  `correct_answers` int(11) DEFAULT NULL,
  `accuracy` decimal(5,2) DEFAULT NULL,
  `submitted_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `source` varchar(50) NOT NULL DEFAULT 'local',
  `attempt_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 傾印資料表的資料 `quiz_results`
--

INSERT INTO `quiz_results` (`id`, `user_id`, `video_id`, `total_questions`, `correct_answers`, `accuracy`, `submitted_at`, `source`, `attempt_id`) VALUES
(40, 123, 1, 5, 1, '20.00', '2025-06-21 22:34:54', 'local', 1750574094424),
(41, 123, 2, 5, 1, '20.00', '2025-06-21 22:38:14', 'local', 1750574294917);

-- --------------------------------------------------------

--
-- 資料表結構 `videos`
--

CREATE TABLE `videos` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text,
  `duration_seconds` int(11) DEFAULT NULL,
  `subject` varchar(100) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `video_url` varchar(500) DEFAULT NULL,
  `youtube_id` varchar(100) DEFAULT NULL,
  `type` varchar(50) DEFAULT 'general'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 傾印資料表的資料 `videos`
--

INSERT INTO `videos` (`id`, `created_at`, `description`, `duration_seconds`, `subject`, `title`, `video_url`, `youtube_id`, `type`) VALUES
(1, '2025-06-23 16:09:42.000000', '用最簡單的方式跟各位介紹CSS', NULL, NULL, 'CSS基礎教學 #1 - 什麼是CSS', 'https://www.youtube.com/watch?v=6peRhh_ASQw', '6peRhh_ASQw', 'general'),
(2, '2025-06-23 16:09:42.000000', 'button簡介', NULL, NULL, '【 HTML 】 Button', 'https://www.youtube.com/watch?v=kQBN4LyXTrk', 'kQBN4LyXTrk', 'general'),
(3, '2025-06-23 16:09:42.000000', '從0開始教。如果你對Javascript有興趣，可以看看這些影片。', NULL, NULL, '10. 基本Javascript教學 | Glitch 線上編輯器 | 下班學程式(JS新手秘笈)', 'https://www.youtube.com/watch?v=M5yL-ZrI35k', 'M5yL-ZrI35k', 'general');

-- --------------------------------------------------------

--
-- 資料表結構 `video_chat_sessions`
--

CREATE TABLE `video_chat_sessions` (
  `video_id` bigint(20) NOT NULL,
  `session_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `video_session_links`
--

CREATE TABLE `video_session_links` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `end_time_seconds` int(11) DEFAULT NULL,
  `last_viewed_time_seconds` int(11) DEFAULT NULL,
  `start_time_seconds` int(11) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `session_id` bigint(20) NOT NULL,
  `video_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `watch_progress`
--

CREATE TABLE `watch_progress` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `video_id` bigint(20) NOT NULL,
  `current_time_sec` int(11) DEFAULT '0',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 已傾印資料表的索引
--

--
-- 資料表索引 `answer`
--
ALTER TABLE `answer`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `quiz_id` (`quiz_id`),
  ADD KEY `video_id` (`video_id`);

--
-- 資料表索引 `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `member_id` (`member_id`),
  ADD KEY `session_id` (`session_id`);

--
-- 資料表索引 `chat_sessions`
--
ALTER TABLE `chat_sessions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `member_id` (`member_id`),
  ADD KEY `video_id` (`video_id`);

--
-- 資料表索引 `members`
--
ALTER TABLE `members`
  ADD PRIMARY KEY (`id`);

--
-- 資料表索引 `quiz`
--
ALTER TABLE `quiz`
  ADD PRIMARY KEY (`quiz_id`),
  ADD KEY `video_id` (`video_id`);

--
-- 資料表索引 `quiz_results`
--
ALTER TABLE `quiz_results`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `video_id` (`video_id`);

--
-- 資料表索引 `videos`
--
ALTER TABLE `videos`
  ADD PRIMARY KEY (`id`);

--
-- 資料表索引 `video_chat_sessions`
--
ALTER TABLE `video_chat_sessions`
  ADD KEY `FK1v74utmyaff3q1jhvu613fdbk` (`session_id`),
  ADD KEY `FKbj3waek256kkbhtnn6g22f6kv` (`video_id`);

--
-- 資料表索引 `video_session_links`
--
ALTER TABLE `video_session_links`
  ADD PRIMARY KEY (`id`),
  ADD KEY `session_id` (`session_id`),
  ADD KEY `video_id` (`video_id`);

--
-- 資料表索引 `watch_progress`
--
ALTER TABLE `watch_progress`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_video` (`user_id`,`video_id`),
  ADD UNIQUE KEY `UKkbc7510yij53hq2bt7dcd7dnv` (`user_id`,`video_id`),
  ADD KEY `video_id` (`video_id`);

--
-- 在傾印的資料表使用自動遞增(AUTO_INCREMENT)
--

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `answer`
--
ALTER TABLE `answer`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=506;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `chat_messages`
--
ALTER TABLE `chat_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `chat_sessions`
--
ALTER TABLE `chat_sessions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `quiz`
--
ALTER TABLE `quiz`
  MODIFY `quiz_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=812;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `quiz_results`
--
ALTER TABLE `quiz_results`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `videos`
--
ALTER TABLE `videos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `video_session_links`
--
ALTER TABLE `video_session_links`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `watch_progress`
--
ALTER TABLE `watch_progress`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 已傾印資料表的限制式
--

--
-- 資料表的限制式 `answer`
--
ALTER TABLE `answer`
  ADD CONSTRAINT `answer_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `members` (`id`),
  ADD CONSTRAINT `answer_ibfk_2` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`quiz_id`),
  ADD CONSTRAINT `answer_ibfk_3` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`);

--
-- 資料表的限制式 `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`),
  ADD CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`);

--
-- 資料表的限制式 `chat_sessions`
--
ALTER TABLE `chat_sessions`
  ADD CONSTRAINT `chat_sessions_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`),
  ADD CONSTRAINT `chat_sessions_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`);

--
-- 資料表的限制式 `quiz`
--
ALTER TABLE `quiz`
  ADD CONSTRAINT `quiz_ibfk_1` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`);

--
-- 資料表的限制式 `quiz_results`
--
ALTER TABLE `quiz_results`
  ADD CONSTRAINT `quiz_results_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `members` (`id`),
  ADD CONSTRAINT `quiz_results_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`);

--
-- 資料表的限制式 `video_chat_sessions`
--
ALTER TABLE `video_chat_sessions`
  ADD CONSTRAINT `FK1v74utmyaff3q1jhvu613fdbk` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`),
  ADD CONSTRAINT `FKbj3waek256kkbhtnn6g22f6kv` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`);

--
-- 資料表的限制式 `video_session_links`
--
ALTER TABLE `video_session_links`
  ADD CONSTRAINT `video_session_links_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`),
  ADD CONSTRAINT `video_session_links_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`);

--
-- 資料表的限制式 `watch_progress`
--
ALTER TABLE `watch_progress`
  ADD CONSTRAINT `watch_progress_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `members` (`id`),
  ADD CONSTRAINT `watch_progress_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
