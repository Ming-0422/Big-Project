const chat = document.getElementById('chat');
const input = document.getElementById('userInput');
const send = document.getElementById('send');

function sendMessage(fromUser = true, text = '') {
    const content = text || input.value.trim();
    if (content !== '') {
        const msg = document.createElement('div');
        msg.className = 'message ' + (fromUser ? 'user' : 'bot');
        msg.textContent = content;
        chat.appendChild(msg);

        if (fromUser) input.value = '';

        chat.scrollTop = chat.scrollHeight;
    }
}

function mockGPTReply(userText) {
    return new Promise(resolve => {
        setTimeout(() => {
            resolve('GPT 模擬回覆：' + userText);
        }, 1000);
    });
}

async function handleSend() {
    const userText = input.value.trim();
    if (!userText) return;

    sendMessage(true, userText);

    try {
        const reply = await mockGPTReply(userText);
        sendMessage(false, reply);
    } catch (error) {
        sendMessage(false, '伺服器錯誤，請稍後再試');
    }
}

input.addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        handleSend();
    }
});

send.addEventListener('click', handleSend);
