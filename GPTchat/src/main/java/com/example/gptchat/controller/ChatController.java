package com.example.gptchat.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gptchat.dto.ChatMessageDTO;
import com.example.gptchat.dto.ChatSessionDTO;
import com.example.gptchat.entity.ChatMessage;
import com.example.gptchat.entity.ChatSession;
import com.example.gptchat.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createChatSession(@RequestBody Map<String, Object> payload) {
        Long memberId = Long.valueOf(payload.get("memberId").toString());
        String title = (String) payload.get("title");
        ChatSession newSession = chatService.createNewSession(memberId, title);
        return ResponseEntity.ok(newSession);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDTO>> getMemberChatSessions(@RequestParam Long memberId) {
        List<ChatSessionDTO> sessions = chatService.getMemberSessions(memberId);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, Object> payload) {
        Long memberId = Long.valueOf(payload.get("memberId").toString());
        Long sessionId = Long.valueOf(payload.get("sessionId").toString());
        String userMessage = (String) payload.get("message");
        
        // 單一方法完成所有操作：取得回覆、儲存對話、更新標題
        String aiResponse = chatService.getOpenAIResponseAndSave(memberId, sessionId, userMessage);
        
        return ResponseEntity.ok(aiResponse);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistoryBySession(@RequestParam Long sessionId) {
        List<ChatMessage> history = chatService.getChatHistoryBySession(sessionId);
        List<ChatMessageDTO> dtoList = history.stream()
            .map(ChatMessageDTO::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/updateTitle")
    public ResponseEntity<String> updateSessionTitle(@RequestBody Map<String, Object> payload) {
        Long sessionId = Long.valueOf(payload.get("sessionId").toString());
        String newTitle = (String) payload.get("title");
        
        boolean updated = chatService.updateSessionTitle(sessionId, newTitle);
        if (updated) {
            return ResponseEntity.ok("標題更新成功");
        } else {
            return ResponseEntity.badRequest().body("標題更新失敗");
        }
    }

    @PostMapping("/rename")
    public ResponseEntity<String> renameSession(@RequestBody Map<String, Object> payload) {
        Long sessionId = Long.valueOf(payload.get("sessionId").toString());
        String newTitle = (String) payload.get("title");
        
        boolean updated = chatService.updateSessionTitle(sessionId, newTitle);
        if (updated) {
            return ResponseEntity.ok("會話重新命名成功");
        } else {
            return ResponseEntity.badRequest().body("會話重新命名失敗");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteSession(@RequestBody Map<String, Object> payload) {
        Long sessionId = Long.valueOf(payload.get("sessionId").toString());
        
        boolean deleted = chatService.deleteSession(sessionId);
        if (deleted) {
            return ResponseEntity.ok("會話刪除成功");
        } else {
            return ResponseEntity.badRequest().body("會話刪除失敗");
        }
    }
}