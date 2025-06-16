package com.example.gptchat.entity; // 注意這裡的包名是小寫的 entity

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages") // 映射到資料庫的 chat_messages 表
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動增長主鍵
    private Long id;

    // Many-to-One 關係：多條 ChatMessage 屬於一個 Member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // 外鍵列名，不能為空
    private Member member; // 注意這裡引用的是 com.example.gptchat.entity.Member

    // 新增 Many-to-One 關係：多條 ChatMessage 屬於一個 ChatSession
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id") // 外鍵列名，可以為空（如果某些訊息不屬於特定對話）
    private ChatSession session; // 注意這裡引用的是 com.example.gptchat.entity.ChatSession

    @Enumerated(EnumType.STRING) // 將 Java 的 Enum 映射為資料庫的 String 類型
    @Column(name = "role", nullable = false) // 角色列名，不能為空
    private Role role; // 'USER' or 'ASSISTANT'

    @Column(columnDefinition = "TEXT", nullable = false) // 訊息內容，使用 TEXT 類型，不能為空
    private String message;

    @Column(name = "timestamp", nullable = false) // 時間戳記，不能為空
    private LocalDateTime timestamp;

    public enum Role {
        USER, ASSISTANT
    }

    // Constructors (建構子)
    public ChatMessage() {
        this.timestamp = LocalDateTime.now(); // 預設時間戳記為當前時間
    }

    public ChatMessage(Member member, ChatSession session, Role role, String message) {
        this.member = member;
        this.session = session; // 新增 session 參數
        this.role = role;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters (取值與設定方法)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    // 新增 session 的 Getter 和 Setter
    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @PrePersist // 在物件持久化到資料庫前執行
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}