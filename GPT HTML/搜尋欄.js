const chat = document.getElementById('chat');
const input = document.getElementById('userInput');
const send = document.getElementById('send');

// 將訊息加入聊天室的函數
function sendMessage() {
    if (input.value.trim() !== '') {
        const msg = document.createElement('div');
        msg.className = 'message';
        msg.textContent = input.value;

        chat.append(msg);
        input.value = ''; // 清空輸入框
    }
}

// 按 Enter 鍵送出訊息
input.addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
});

// 點擊箭頭按鈕送出訊息
send.addEventListener('click', sendMessage);


