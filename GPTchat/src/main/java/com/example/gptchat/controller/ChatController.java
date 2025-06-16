package com.example.gptchat.controller;

import com.example.gptchat.entity.ChatMessage;
import com.example.gptchat.entity.ChatSession;
import com.example.gptchat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // 用於接收 JSON 請求體

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    // 構造函數注入
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // --- 新增會話相關 API ---

    // 創建新會話
    // POST /api/chat/sessions
    // Request Body: { "memberId": 1, "title": "我的第一個聊天" }
    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createChatSession(@RequestBody Map<String, Object> payload) {
        Long memberId = Long.valueOf(payload.get("memberId").toString());
        String title = (String) payload.get("title");
        ChatSession newSession = chatService.createNewSession(memberId, title);
        return ResponseEntity.ok(newSession);
    }

    // 獲取某個會員的所有會話列表
    // GET /api/chat/sessions?memberId=1
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getMemberChatSessions(@RequestParam Long memberId) {
        List<ChatSession> sessions = chatService.getMemberSessions(memberId);
        return ResponseEntity.ok(sessions);
    }

    // --- 修改發送消息 API，使其與會話關聯 ---

    // 向特定會話發送消息
    // POST /api/chat/send
    // Request Body: { "memberId": 1, "sessionId": 123, "message": "你好，AI！" }
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, Object> payload) {
        Long memberId = Long.valueOf(payload.get("memberId").toString());
        // sessionId 可以為 null，如果前端不傳或傳 null，則表示不關聯特定會話
        Long sessionId = null;
        if (payload.containsKey("sessionId") && payload.get("sessionId") != null) {
            sessionId = Long.valueOf(payload.get("sessionId").toString());
        }
        String userMessage = (String) payload.get("message");

        // 調用 ChatService 處理 AI 回覆和消息儲存
        String aiResponse = chatService.getOpenAIResponse(memberId, sessionId, userMessage);
        return ResponseEntity.ok(aiResponse);
    }

    // 獲取特定會話的聊天歷史
    // GET /api/chat/history?sessionId=123
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistoryBySession(@RequestParam Long sessionId) {
        List<ChatMessage> history = chatService.getChatHistoryBySession(sessionId);
        return ResponseEntity.ok(history);
    }

    // 舊的獲取所有訊息歷史 (如果還需要的話)
    // GET /api/chat/all-history?memberId=1
    @GetMapping("/all-history")
    public ResponseEntity<List<ChatMessage>> getAllChatHistory(@RequestParam Long memberId) {
        List<ChatMessage> history = chatService.getChatHistory(memberId);
        return ResponseEntity.ok(history);
    }
}