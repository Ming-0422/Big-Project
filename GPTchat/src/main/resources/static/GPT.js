// 檔案位置: src/main/resources/static/GPT.js
// 取得 DOM 元素
const chat = document.getElementById('chat');
const input = document.getElementById('userInput');
const send = document.getElementById('send');
const iconArrow = send.querySelector('.icon-arrow');
const sidebar = document.getElementById('sidebar');
const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
const recentChatsList = document.getElementById('recentChatsList');
const newChatBtn = document.querySelector('.new-chat-btn');

let currentSessionId = null;
const TEST_MEMBER_ID = 1; // 假設登入的會員ID為1
let currentContextSessionId = null; // 右鍵選單當前會話ID

// --- 右鍵選單功能 ---
function initializeContextMenu() {
    const contextMenu = document.getElementById('contextMenu');
    let isMenuVisible = false;

    // 隱藏右鍵選單
    function hideContextMenu() {
        contextMenu.style.display = 'none';
        isMenuVisible = false;
        currentContextSessionId = null;
    }

    // 顯示右鍵選單
    function showContextMenu(e, sessionId) {
        e.preventDefault();
        currentContextSessionId = sessionId;
        
        contextMenu.style.display = 'block';
        contextMenu.style.left = e.clientX + 'px';
        contextMenu.style.top = e.clientY + 'px';
        isMenuVisible = true;

        // 調整位置以確保選單不會超出視窗
        const rect = contextMenu.getBoundingClientRect();
        if (rect.right > window.innerWidth) {
            contextMenu.style.left = (e.clientX - rect.width) + 'px';
        }
        if (rect.bottom > window.innerHeight) {
            contextMenu.style.top = (e.clientY - rect.height) + 'px';
        }
    }

    // 點擊其他地方隱藏選單
    document.addEventListener('click', (e) => {
        if (isMenuVisible && !contextMenu.contains(e.target)) {
            hideContextMenu();
        }
    });

    // 右鍵選單項目點擊處理
    contextMenu.addEventListener('click', async (e) => {
        console.log('Context menu clicked:', e.target);
        const item = e.target.closest('.context-menu-item');
        console.log('Menu item found:', item);
        console.log('Current session ID:', currentContextSessionId);
        
        if (!item || !currentContextSessionId) {
            console.log('Missing item or session ID, returning');
            return;
        }

        const action = item.dataset.action;
        const sessionId = currentContextSessionId; // 捕獲 ID
        console.log('Action:', action);
        hideContextMenu(); // 現在隱藏是安全的

        try {
            if (action === 'rename') {
                console.log('Executing rename action');
                await showRenameDialog(sessionId); // 使用捕獲的 ID
            } else if (action === 'delete') {
                console.log('Executing delete action');
                await showDeleteConfirmation(sessionId); // 使用捕獲的 ID
            } else {
                console.log('Unknown action:', action);
            }
        } catch (error) {
            console.error('Error executing context menu action:', error);
            showNotification('操作失敗：' + error.message, 'error');
        }
    });

    return { showContextMenu };
}

// 重新命名對話框
async function showRenameDialog(sessionId) {
    const linkElement = document.querySelector(`a.chat-link[data-session-id="${sessionId}"]`);
    if (!linkElement) return;

    const originalText = linkElement.textContent.replace('[已儲存] ', '');
    const listItem = linkElement.closest('li');

    // 創建重新命名界面
    const renameContainer = document.createElement('div');
    renameContainer.className = 'rename-container';
    renameContainer.innerHTML = `
        <input type="text" class="rename-input" value="${originalText}" />
        <div class="rename-buttons">
            <button class="rename-btn cancel">取消</button>
            <button class="rename-btn confirm">確認</button>
        </div>
    `;

    // 替換連結內容
    const originalLinkContent = linkElement.innerHTML;
    linkElement.innerHTML = '';
    linkElement.appendChild(renameContainer);
    linkElement.style.pointerEvents = 'none'; // 禁用點擊

    const input = renameContainer.querySelector('.rename-input');
    const cancelBtn = renameContainer.querySelector('.cancel');
    const confirmBtn = renameContainer.querySelector('.confirm');

    // 自動選中文字
    input.focus();
    input.select();

    // 取消重新命名
    function cancelRename() {
        linkElement.innerHTML = originalLinkContent;
        linkElement.style.pointerEvents = 'auto';
    }

    // 確認重新命名
    async function confirmRename() {
        const newTitle = input.value.trim();
        console.log('Confirming rename with title:', newTitle);
        
        if (!newTitle) {
            console.log('Empty title, cancelling rename');
            cancelRename();
            return;
        }

        try {
            console.log('Sending rename request for session:', sessionId);
            const response = await fetch('http://localhost:8080/api/chat/rename', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    sessionId: parseInt(sessionId), 
                    title: newTitle 
                })
            });

            console.log('Rename response status:', response.status);
            
            if (response.ok) {
                const responseText = await response.text();
                console.log('Rename response:', responseText);
                
                // 重新載入會話列表
                await loadRecentSessions();
                showNotification('會話重新命名成功', 'success');
                
                // 更新聊天標題如果是當前會話
                if (parseInt(sessionId) === currentSessionId) {
                    document.querySelector('.chat-title').textContent = newTitle;
                }
            } else {
                const errorText = await response.text();
                console.error('Rename failed with response:', errorText);
                throw new Error('重新命名失敗: ' + errorText);
            }
        } catch (error) {
            console.error('重新命名失敗:', error);
            showNotification('重新命名失敗，請稍後再試: ' + error.message, 'error');
            cancelRename();
        }
    }

    // 綁定事件
    cancelBtn.addEventListener('click', cancelRename);
    confirmBtn.addEventListener('click', confirmRename);
    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            confirmRename();
        } else if (e.key === 'Escape') {
            cancelRename();
        }
    });
}

// 刪除確認對話框
async function showDeleteConfirmation(sessionId) {
    console.log('Showing delete confirmation for session:', sessionId);
    const linkElement = document.querySelector(`a.chat-link[data-session-id="${sessionId}"]`);
    console.log('Found link element:', linkElement);
    
    if (!linkElement) {
        console.error('Link element not found for session:', sessionId);
        showNotification('無法找到會話，請重新整理頁面', 'error');
        return;
    }

    const sessionTitle = linkElement.textContent.replace('[已儲存] ', '');
    console.log('Session title:', sessionTitle);
    
    if (confirm(`確定要刪除會話「${sessionTitle}」嗎？\n此操作無法復原。`)) {
        try {
            console.log('Sending delete request for session:', sessionId);
            const response = await fetch('http://localhost:8080/api/chat/delete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ sessionId: parseInt(sessionId) })
            });

            console.log('Delete response status:', response.status);

            if (response.ok) {
                const responseText = await response.text();
                console.log('Delete response:', responseText);
                showNotification('會話刪除成功', 'success');
                
                // 如果刪除的是當前會話，切換到新會話
                if (parseInt(sessionId) === currentSessionId) {
                    console.log('Deleted current session, clearing chat');
                    currentSessionId = null;
                    clearChatMessages();
                    document.querySelector('.chat-title').textContent = 'GPT 聊天';
                }
                
                // 重新載入會話列表
                await loadRecentSessions();
            } else {
                const errorText = await response.text();
                console.error('Delete failed with response:', errorText);
                throw new Error('刪除失敗: ' + errorText);
            }
        } catch (error) {
            console.error('刪除會話失敗:', error);
            showNotification('刪除失敗，請稍後再試: ' + error.message, 'error');
        }
    } else {
        console.log('User cancelled delete operation');
    }
}

// 通知提示
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    // 3秒後自動移除
    setTimeout(() => {
        notification.style.opacity = '0';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

// --- 核心功能 ---

async function handleSend() {
    const userText = input.value.trim();
    if (!userText || currentSessionId === null) return;

    const sendButtonContent = send.innerHTML;
    send.innerHTML = `<span class="loading-icon"></span>`;
    send.disabled = true;
    input.disabled = true;

    sendMessage(true, userText);

    const thinkingMsg = document.createElement('div');
    thinkingMsg.className = 'message bot';
    thinkingMsg.id = 'thinkingMessage';
    thinkingMsg.innerHTML = `正在思考中...<span class="blinking-cursor"></span>`;
    chat.appendChild(thinkingMsg);
    chat.scrollTop = chat.scrollHeight;

    try {
        const response = await fetch('http://localhost:8080/api/chat/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ memberId: TEST_MEMBER_ID, sessionId: currentSessionId, message: userText })
        });

        if (!response.ok) throw new Error(`伺服器錯誤: ${response.statusText}`);

        const reply = await response.text();
        const thinkingElement = document.getElementById('thinkingMessage');
        if (thinkingElement) {
            thinkingElement.innerHTML = reply;
            thinkingElement.removeAttribute('id');
        }
        
        // 標題可能已更新，短暫延遲後重新載入側邊欄
        setTimeout(loadRecentSessions, 1000);

    } catch (error) {
        console.error('發送消息時出錯:', error);
        const thinkingElement = document.getElementById('thinkingMessage');
        if (thinkingElement) {
            thinkingElement.textContent = '發生錯誤，請稍後再試。';
        }
    } finally {
        send.disabled = false;
        input.disabled = false;
        send.innerHTML = sendButtonContent;
        chat.scrollTop = chat.scrollHeight;
        input.focus();
    }
}

async function createNewChatSession() {
    try {
        const response = await fetch('http://localhost:8080/api/chat/sessions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ memberId: TEST_MEMBER_ID, title: "新對話 " + new Date().toLocaleString() })
        });
        if (!response.ok) throw new Error('無法創建新的聊天會話');
        const sessionData = await response.json();
        await loadRecentSessions();
        switchChatSession(sessionData.id);
    } catch (error) {
        console.error('創建新會話失敗:', error);
        sendMessage(false, '創建新對話失敗。');
    }
}

async function loadRecentSessions() {
    try {
        const response = await fetch(`http://localhost:8080/api/chat/sessions?memberId=${TEST_MEMBER_ID}`);
        if (!response.ok) throw new Error('無法獲取近期對話');
        const sessions = await response.json();
        recentChatsList.innerHTML = '';

        // 初始化右鍵選單
        const contextMenuHandler = initializeContextMenu();

        sessions.forEach(session => {
            const listItem = document.createElement('li');
            listItem.className = 'chat-item';
            
            // 創建聊天項目容器
            const chatItemContainer = document.createElement('div');
            chatItemContainer.className = 'chat-item-container';
            
            // 創建主要的聊天連結
            const link = document.createElement('a');
            link.href = '#';
            link.className = 'chat-link';
            link.textContent = session.title || `對話 #${session.id}`;
            link.dataset.sessionId = session.id;
            if (session.id === currentSessionId) {
                link.classList.add('active');
            }
            link.onclick = (e) => { 
                e.preventDefault(); 
                switchChatSession(session.id); 
            };
            
            // 創建三個點選項按鈕
            const optionsBtn = document.createElement('button');
            optionsBtn.className = 'chat-options-btn';
            optionsBtn.innerHTML = '⋯';
            optionsBtn.title = '選項';
            
            // 點擊三個點按鈕顯示右鍵選單
            optionsBtn.onclick = (e) => {
                e.preventDefault();
                e.stopPropagation();
                contextMenuHandler.showContextMenu(e, session.id);
            };
            
            // 添加右鍵選單事件到整個聊天項目
            chatItemContainer.addEventListener('contextmenu', (e) => {
                contextMenuHandler.showContextMenu(e, session.id);
            });
            
            // 組裝元素
            chatItemContainer.appendChild(link);
            chatItemContainer.appendChild(optionsBtn);
            listItem.appendChild(chatItemContainer);
            recentChatsList.appendChild(listItem);
        });

        if (!currentSessionId && sessions.length > 0) {
            switchChatSession(sessions[0].id);
        } else if (sessions.length === 0) {
            createNewChatSession();
        }
    } catch (error) {
        console.error('載入近期對話失敗:', error);
    }
}

async function switchChatSession(sessionId) {
    if (currentSessionId === sessionId && chat.children.length > 0) return;
    
    currentSessionId = sessionId;
    clearChatMessages();
    
    // 更新側邊欄中所有連結的狀態
    document.querySelectorAll('#recentChatsList a.chat-link').forEach(link => {
        link.classList.remove('active');
        if (link.dataset.sessionId == sessionId) {
            link.classList.add('active');
        }
    });

    try {
        const response = await fetch(`http://localhost:8080/api/chat/history?sessionId=${sessionId}`);
        if (!response.ok) throw new Error('無法載入聊天歷史');
        const messages = await response.json();
        
        // 清空現有訊息
        clearChatMessages();
        
        // 載入歷史訊息
        messages.forEach(msg => {
            const isUser = msg.role === 'USER';
            sendMessage(isUser, msg.message);
        });
        
        // 滾動到底部
        chat.scrollTop = chat.scrollHeight;
        
        // 更新聊天標題
        const sessionResponse = await fetch(`http://localhost:8080/api/chat/sessions?memberId=${TEST_MEMBER_ID}`);
        if (sessionResponse.ok) {
            const sessions = await sessionResponse.json();
            const currentSession = sessions.find(s => s.id === sessionId);
            if (currentSession) {
                document.querySelector('.chat-title').textContent = currentSession.title || 'GPT 聊天';
            }
        }
    } catch (error) {
        console.error('載入聊天歷史失敗:', error);
        sendMessage(false, '載入歷史訊息失敗。');
    }
}

// --- UI 輔助功能 ---

function sendMessage(fromUser, text) {
    const msg = document.createElement('div');
    msg.className = 'message ' + (fromUser ? 'user' : 'bot');
    msg.innerHTML = text; // 使用 innerHTML 以便渲染換行等
    chat.appendChild(msg);
    if (fromUser) input.value = '';
    chat.scrollTop = chat.scrollHeight;
}

function clearChatMessages() {
    chat.innerHTML = '';
}

function toggleSidebar() {
    sidebar.classList.toggle('collapsed');
}

// --- 事件監聽 ---
input.addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        handleSend();
    }
});
send.addEventListener('click', handleSend);
sidebarToggleBtn.addEventListener('click', toggleSidebar);
newChatBtn.addEventListener('click', createNewChatSession);

// --- 頁面初始化 ---
document.addEventListener('DOMContentLoaded', () => {
    // 檢查URL參數是否有指定會話ID
    const urlParams = new URLSearchParams(window.location.search);
    const sessionIdFromUrl = urlParams.get('sessionId');
    
    if (sessionIdFromUrl) {
        // 如果有指定會話ID，載入該會話
        loadRecentSessions().then(() => {
            switchChatSession(parseInt(sessionIdFromUrl));
        });
    } else {
        // 正常載入最近會話
        loadRecentSessions();
    }
});