package com.example.gptchat.service;

import com.example.gptchat.entity.ChatMessage;
import com.example.gptchat.entity.ChatSession;
import com.example.gptchat.entity.Member;
import com.example.gptchat.repository.ChatMessageRepository;
import com.example.gptchat.repository.ChatSessionRepository;
import com.example.gptchat.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient; // 引入 WebClient
import reactor.core.publisher.Mono; // 引入 Mono

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList; // 引入 ArrayList for message list

@Service
public class ChatService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final WebClient webClient; // 新增 WebClient 實例

    // 更新構造函數，注入 WebClient.Builder 來建立 WebClient
    public ChatService(ChatMessageRepository chatMessageRepository,
                       MemberRepository memberRepository,
                       ChatSessionRepository chatSessionRepository,
                       WebClient.Builder webClientBuilder) { // 注入 Builder
        this.chatMessageRepository = chatMessageRepository;
        this.memberRepository = memberRepository;
        this.chatSessionRepository = chatSessionRepository;
        // 建立 WebClient 實例，設定基礎 URL
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1/chat/completions").build();
    }

    // --- 會話管理方法 (維持不變) ---
    @Transactional
    public ChatSession createNewSession(Long memberId, String title) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));
        ChatSession session = new ChatSession(member, title);
        return chatSessionRepository.save(session);
    }

    public List<ChatSession> getMemberSessions(Long memberId) {
        return chatSessionRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    public Optional<ChatSession> getSessionById(Long sessionId) {
        return chatSessionRepository.findById(sessionId);
    }

    // --- 儲存消息方法 (維持不變) ---
    @Transactional
    public ChatMessage saveMessage(Long memberId, Long sessionId, String message, ChatMessage.Role role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        ChatSession session = null;
        if (sessionId != null) {
            session = chatSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("ChatSession not found with id: " + sessionId));
            if (!session.getMember().getId().equals(memberId)) {
                throw new IllegalArgumentException("Session does not belong to the specified member.");
            }
        }
        ChatMessage chatMessage = new ChatMessage(member, session, role, message);
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatHistoryBySession(Long sessionId) {
        return chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    // --- 核心修改：OpenAI 呼叫邏輯 ---
    /**
     * 處理使用者訊息，呼叫 OpenAI API 取得回覆，並儲存對話。
     * @param memberId 會員 ID
     * @param sessionId 會話 ID
     * @param userMessage 使用者發送的訊息
     * @return 來自 OpenAI 的回覆
     */
    @Transactional
    public String getOpenAIResponse(Long memberId, Long sessionId, String userMessage) {
        // 1. 儲存使用者訊息 (此步驟維持不變)
        saveMessage(memberId, sessionId, userMessage, ChatMessage.Role.USER);

        // 2. 獲取當前會話的歷史訊息 (用於傳遞給 OpenAI 進行多輪對話上下文)
        List<ChatMessage> history = getChatHistoryBySession(sessionId);

        // 3. 構建 OpenAI API 請求體
        List<Message> messagesForOpenAI = history.stream()
                .map(msg -> new Message(msg.getRole().name().toLowerCase(), msg.getMessage()))
                .collect(Collectors.toList());
        
        // 4. 使用 WebClient 呼叫真實的 OpenAI API
        ChatRequest requestBody = new ChatRequest("gpt-4o", messagesForOpenAI);

        try {
            ChatResponse response = webClient.post()
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve() // 發送請求並準備接收響應
                    .bodyToMono(ChatResponse.class) // 將響應體轉換為 ChatResponse 物件
                    .block(); // 等待並獲取結果 (在服務層中使用 block 是可接受的簡化方法)

            String aiResponseContent;
            if (response != null && !response.choices.isEmpty()) {
                aiResponseContent = response.choices.get(0).message.content.trim();
            } else {
                aiResponseContent = "抱歉，AI 沒有提供有效的回覆。";
            }

            // 5. 儲存 AI 回覆
            saveMessage(memberId, sessionId, aiResponseContent, ChatMessage.Role.ASSISTANT);

            return aiResponseContent;

        } catch (Exception e) {
            // 實際應用中應該有更完善的日誌和錯誤處理
            System.err.println("呼叫 OpenAI API 時發生錯誤: " + e.getMessage());
            // 也可以選擇在這裡儲存一條錯誤訊息到資料庫
            saveMessage(memberId, sessionId, "API 呼叫失敗，請稍後再試。", ChatMessage.Role.ASSISTANT);
            return "對不起，我現在無法回答您的問題。";
        }
    }

    // --- 用於構建 OpenAI 請求和響應的輔助內部類 ---
    private static class ChatRequest {
        public String model;
        public List<Message> messages;

        public ChatRequest(String model, List<Message> messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    private static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private static class ChatResponse {
        public List<Choice> choices;

        private static class Choice {
            public Message message;
        }
    }
    
    // 舊的 getChatHistory 方法 (維持不變)
    public List<ChatMessage> getChatHistory(Long memberId) {
        return chatMessageRepository.findByMemberIdOrderByTimestampAsc(memberId);
    }
}