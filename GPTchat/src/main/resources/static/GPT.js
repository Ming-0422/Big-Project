// 取得 DOM 元素
const chat = document.getElementById('chat');
const input = document.getElementById('userInput');
const send = document.getElementById('send');
const iconArrow = send.querySelector('.icon-arrow');

// 新增：側邊欄相關 DOM 元素
const sidebar = document.getElementById('sidebar');
const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
const recentChatsList = document.getElementById('recentChatsList'); // 近期對話列表的 UL 元素
const newChatBtn = document.querySelector('.new-chat-btn'); // 「新的對話」按鈕

// 產生唯一 session ID，用來維持同一次對話紀錄
let currentSessionId = null; // 當前活動的 session ID
const TEST_MEMBER_ID = 1; // 測試用會員ID，實際應用中應從登入狀態獲取

/**
 * 新增訊息到聊天區
 * @param {boolean} fromUser - 是否為使用者訊息(true)或機器人(false)
 * @param {string} text - 訊息內容
 */
function sendMessage(fromUser = true, text = '') {
	const content = text || input.value.trim();
	if (content !== '') {
		const msg = document.createElement('div');
		msg.className = 'message ' + (fromUser ? 'user' : 'bot');
		msg.textContent = content;
		chat.appendChild(msg);
		if (fromUser) input.value = ''; // 使用者訊息送出後清空輸入框
		chat.scrollTop = chat.scrollHeight; // 自動捲動到最底部
	}
}

/**
 * 清空聊天區塊的訊息
 */
function clearChatMessages() {
	chat.innerHTML = '';
}

/**
 * 處理送出行為 (發送消息到後端)
 */
async function handleSend() {
	const userText = input.value.trim();
	if (!userText || currentSessionId === null) return; // 空字串或無 session ID 不送出

	iconArrow.style.display = 'none';
	const loading = document.createElement('span');
	loading.className = 'loading-icon';
	loading.id = 'loadingIcon';
	send.appendChild(loading);
	send.disabled = true;

	sendMessage(true, userText); // 先顯示使用者訊息

	try {
		// 發送 POST 請求到後端，帶入使用者問題及 sessionId
		// 注意：這裡假設您的後端 API 已經有處理 memberId, sessionId 和 message 的邏輯
		const response = await fetch('http://localhost:8080/api/chat/send', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ memberId: TEST_MEMBER_ID, sessionId: currentSessionId, message: userText })
		});

		if (!response.ok) throw new Error('伺服器錯誤');

		const reply = await response.text();
		sendMessage(false, reply); // 顯示 AI 回覆
	} catch (error) {
		console.error('發送消息時出錯:', error);
		sendMessage(false, '伺服器錯誤，請稍後再試');
	} finally {
		send.disabled = false;
		iconArrow.style.display = '';
		const loadingIcon = document.getElementById('loadingIcon');
		if (loadingIcon) loadingIcon.remove();
	}
}

/**
 * 處理側邊欄收合/展開的邏輯
 */
function toggleSidebar() {
	sidebar.classList.toggle('collapsed');
}

/**
 * 從後端獲取並顯示近期對話列表
 * 需要後端提供 GET /api/chat/sessions?memberId={id} 接口，返回 [ { id: "sessionId1", title: "對話標題1" } ]
 */
async function loadRecentSessions() {
	try {
		const response = await fetch(`http://localhost:8080/api/chat/sessions?memberId=${TEST_MEMBER_ID}`);
		if (!response.ok) throw new Error('無法獲取近期對話');

		const sessions = await response.json();
		recentChatsList.innerHTML = ''; // 清空現有列表

		sessions.forEach(session => {
			const listItem = document.createElement('li');
			const link = document.createElement('a');
			link.href = '#';
			link.textContent = session.title || `對話 #${session.id}`; // 顯示標題或ID
			link.dataset.sessionId = session.id; // 存儲 session ID

			link.addEventListener('click', (e) => {
				e.preventDefault();
				switchChatSession(session.id); // 切換對話
			});
			listItem.appendChild(link);
			recentChatsList.appendChild(listItem);
		});

		// 如果沒有活動會話，且有近期會話，則自動載入最新的會話
		if (!currentSessionId && sessions.length > 0) {
			switchChatSession(sessions[0].id);
		} else if (sessions.length === 0) {
			// 如果沒有任何對話，則自動創建一個新的
			createNewChatSession();
		}

	} catch (error) {
		console.error('載入近期對話失敗:', error);
	}
}

/**
 * 切換到指定 session 的對話
 * 需要後端提供 GET /api/chat/history?sessionId={id} 接口，返回 [ { role: "USER/BOT", message: "內容" } ]
 * @param {string} sessionId - 要切換到的會話ID
 */
async function switchChatSession(sessionId) {
	if (currentSessionId === sessionId) return; // 如果已經是當前會話，則不操作

	currentSessionId = sessionId;
	clearChatMessages(); // 清空當前聊天區

	// 更新側邊欄中當前選中的對話樣式
	const allLinks = recentChatsList.querySelectorAll('a');
	allLinks.forEach(link => {
		if (link.dataset.sessionId == sessionId) {
			link.classList.add('active');
		} else {
			link.classList.remove('active');
		}
	});

	try {
		const response = await fetch(`http://localhost:8080/api/chat/history?sessionId=${sessionId}`);
		if (!response.ok) throw new Error('無法載入聊天歷史');

		const messages = await response.json();
		messages.forEach(msg => sendMessage(msg.role === 'USER', msg.message));
		chat.scrollTop = chat.scrollHeight; // 載入後捲動到底部
	} catch (error) {
		console.error('載入聊天歷史失敗:', error);
		sendMessage(false, '載入歷史訊息失敗。');
	}
}

/**
 * 創建一個新的聊天會話
 * 需要後端提供 POST /api/chat/sessions 接口，接收 { memberId, title }，返回新會話的 { id, title }
 */
async function createNewChatSession() {
	try {
		const response = await fetch('http://localhost:8080/api/chat/sessions', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ memberId: TEST_MEMBER_ID, title: "新對話 " + new Date().toLocaleString() }) // 預設標題
		});

		if (!response.ok) throw new Error('無法創建新的聊天會話');

		const sessionData = await response.json();
		await loadRecentSessions(); // 重新載入側邊欄列表，以便新會話顯示
		switchChatSession(sessionData.id); // 切換到新的會話
		clearChatMessages(); // 清空聊天區，開始新對話
	} catch (error) {
		console.error('創建新會話失敗:', error);
		sendMessage(false, '創建新對話失敗。');
	}
}

// 監聽事件
input.addEventListener('keydown', e => {
	if (e.key === 'Enter' && !e.shiftKey) { // 按 Enter 送出，Shift+Enter 換行
		e.preventDefault();
		handleSend();
	}
});
send.addEventListener('click', handleSend);
sidebarToggleBtn.addEventListener('click', toggleSidebar); // 側邊欄收合/展開
newChatBtn.addEventListener('click', createNewChatSession); // 新的對話按鈕監聽

// 頁面載入完成後初始化
document.addEventListener('DOMContentLoaded', () => {
	loadRecentSessions(); // 載入近期對話列表
});