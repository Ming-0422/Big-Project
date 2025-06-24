// 檔案位置: src/main/java/com/example/gptchat/service/ChatService.java
package com.example.gptchat.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.gptchat.dto.ChatSessionDTO;
import com.example.gptchat.entity.ChatMessage;
import com.example.gptchat.entity.ChatSession;
import com.example.gptchat.entity.Member;
import com.example.gptchat.repository.ChatMessageRepository;
import com.example.gptchat.repository.ChatSessionRepository;
import com.example.gptchat.repository.MemberRepository;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final WebClient webClient;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       MemberRepository memberRepository,
                       ChatSessionRepository chatSessionRepository,
                       WebClient.Builder webClientBuilder) {
        this.chatMessageRepository = chatMessageRepository;
        this.memberRepository = memberRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1/chat/completions").build();
    }

    @Transactional
    public ChatSession createNewSession(Long memberId, String title) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));
        ChatSession session = new ChatSession(member, title);
        logger.info("為會員 {} 創建新會話，標題: {}", memberId, title);
        return chatSessionRepository.save(session);
    }

    public List<ChatSessionDTO> getMemberSessions(Long memberId) {
        List<ChatSession> sessions = chatSessionRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        return sessions.stream()
                .map(session -> new ChatSessionDTO(session.getId(), session.getTitle(), session.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<ChatMessage> getChatHistoryBySession(Long sessionId) {
        return chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    @Transactional
    public boolean updateSessionTitle(Long sessionId, String newTitle) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setTitle(newTitle);
            chatSessionRepository.save(session);
            logger.info("會話 {} 的標題已更新為: {}", sessionId, newTitle);
            return true;
        }
        logger.warn("找不到會話 ID: {}", sessionId);
        return false;
    }

    @Transactional
    public boolean deleteSession(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            // 先刪除該會話的所有聊天訊息
            List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
            if (!messages.isEmpty()) {
                chatMessageRepository.deleteAll(messages);
                logger.info("刪除會話 {} 的 {} 條聊天訊息", sessionId, messages.size());
            }
            
            // 再刪除會話本身
            chatSessionRepository.delete(session);
            logger.info("會話 {} 已成功刪除", sessionId);
            return true;
        }
        logger.warn("找不到會話 ID: {}", sessionId);
        return false;
    }

    @Transactional
    public String getOpenAIResponseAndSave(Long memberId, Long sessionId, String userMessage) {
        // 1. 儲存使用者訊息
        saveMessage(memberId, sessionId, userMessage, ChatMessage.Role.USER);

        // 2. 獲取對話歷史並呼叫OpenAI取得主要回覆
        List<ChatMessage> history = getChatHistoryBySession(sessionId);
        List<Message> messagesForOpenAI = history.stream()
                .map(msg -> new Message(msg.getRole().name().toLowerCase(), msg.getMessage()))
                .collect(Collectors.toList());
        
        // 為每次請求添加系統訊息，確保語言一致性
        messagesForOpenAI.add(0, new Message("system", "請根據用戶的語言來回答問題。如果用戶使用繁體中文，就用繁體中文回答；如果用戶使用簡體中文，就用簡體中文回答；如果用戶使用英文，就用英文回答；如果用戶使用日文，就用日文回答。請始終保持與用戶相同的語言風格。"));
        
        String aiResponseContent = callOpenAI(messagesForOpenAI);

        // 3. 儲存AI的主要回覆
        saveMessage(memberId, sessionId, aiResponseContent, ChatMessage.Role.ASSISTANT);
        
        // 4. 【新功能】檢查並自動生成標題
        updateTitleIfNeeded(sessionId, userMessage, aiResponseContent);

        return aiResponseContent;
    }
    
    // 內部輔助方法，不需要 @Transactional
    private ChatMessage saveMessage(Long memberId, Long sessionId, String message, ChatMessage.Role role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("ChatSession not found with id: " + sessionId));
        ChatMessage chatMessage = new ChatMessage(member, session, role, message);
        return chatMessageRepository.save(chatMessage);
    }
    
    private void updateTitleIfNeeded(Long sessionId, String userMessage, String aiResponseContent) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return;
        }

        // 如果是新對話，生成標題
        if (session.getTitle() != null && session.getTitle().startsWith("新對話")) {
                    logger.info("為會話 {} 生成標題...", sessionId);
        String titlePrompt = String.format(
            "請根據以下對話，為其生成一個不超過15個字的簡潔標題。請只回傳標題文字，不要包含任何引號或多餘解釋。請使用與用戶相同的語言來生成標題。\n\n對話：\n使用者：%s\nAI：%s", 
            userMessage, aiResponseContent
        );
        
        List<Message> titleMessages = Arrays.asList(
            new Message("system", "請根據用戶的語言來回答問題。如果用戶使用繁體中文，就用繁體中文回答；如果用戶使用簡體中文，就用簡體中文回答；如果用戶使用英文，就用英文回答；如果用戶使用日文，就用日文回答。請始終保持與用戶相同的語言風格。"),
            new Message("user", titlePrompt)
        );
            String newTitle = callOpenAI(titleMessages);
            
            // 自動添加 [已儲存] 標記
            session.setTitle(newTitle);
            logger.info("會話 {} 的新標題為: {}", sessionId, newTitle);
        } else if (session.getTitle() != null && !session.getTitle().startsWith("[已儲存]")) {
            // 如果會話已有標題但未標記為已儲存，則添加標記
            String updatedTitle = session.getTitle();
            session.setTitle(updatedTitle);
            logger.info("會話 {} 標記為已儲存: {}", sessionId, updatedTitle);
        }
        
        chatSessionRepository.save(session);
    }

    private String callOpenAI(List<Message> messages) {
        ChatRequest requestBody = new ChatRequest("gpt-4o", messages);
        try {
            ChatResponse response = webClient.post()
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .block();
            if (response != null && !response.choices.isEmpty()) {
                return response.choices.get(0).message.content.trim();
            }
            return "抱歉，AI 沒有提供有效的回覆。";
        } catch (Exception e) {
            logger.error("呼叫 OpenAI API 時發生錯誤", e);
            return "對不起，我現在無法回答您的問題。";
        }
    }

    // --- 用於 OpenAI API 的輔助內部類 (從 GPTService 移入) ---
    private static class ChatRequest {
        public String model;
        public List<Message> messages;
        public ChatRequest(String model, List<Message> messages) { this.model = model; this.messages = messages; }
    }
    private static class Message {
        public String role;
        public String content;
        public Message(String role, String content) { this.role = role; this.content = content; }
    }
    private static class ChatResponse {
        public List<Choice> choices;
        private static class Choice { public Message message; }
    }
}