package com.example.gptchat.dto;

import java.time.LocalDateTime;

import com.example.gptchat.entity.ChatMessage;

public class ChatMessageDTO {
    private Long id;
    private String message;
    private String role;
    private LocalDateTime timestamp;

    public ChatMessageDTO(ChatMessage msg) {
        this.id = msg.getId();
        this.message = msg.getMessage();
        this.role = msg.getRole().name();
        this.timestamp = msg.getTimestamp();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}