package com.example.gptchat.entity; // 注意這裡的包名是小寫的 entity

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List; // For the one-to-many relationship with ChatMessage

@Entity
@Table(name = "chat_sessions") // 映射到資料庫的 chat_sessions 表
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動增長主鍵
    private Long id;

    // Many-to-One 關係：多個 ChatSession 屬於一個 Member
    @ManyToOne(fetch = FetchType.LAZY) // 延遲加載，避免不必要的查詢
    @JoinColumn(name = "member_id", nullable = false) // 外鍵列名，不能為空
    private Member member; // 注意這裡引用的是 com.example.gptchat.entity.Member

    @Column(length = 100) // 標題長度限制
    private String title; // 用於給對話取標題 (例如: "關於Spring Boot的問題", "天氣查詢")

    @Column(name = "created_at", nullable = false) // 創建時間，不能為空
    private LocalDateTime createdAt;

    // One-to-Many 關係：一個 ChatSession 可以有多條 ChatMessage
    // mappedBy 指向 ChatMessage 中擁有 ChatSession 外鍵的屬性名
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    // cascade = CascadeType.ALL 表示對 ChatSession 的操作（如刪除）會級聯到其關聯的 ChatMessage
    // orphanRemoval = true 表示如果一個 ChatMessage 不再被任何 ChatSession 引用，則將其刪除
    private List<ChatMessage> messages;

    // Constructors (建構子)
    public ChatSession() {
        this.createdAt = LocalDateTime.now(); // 預設創建時間為當前時間
    }

    public ChatSession(Member member, String title) {
        this.member = member;
        this.title = title;
        this.createdAt = LocalDateTime.now();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @PrePersist // 在物件持久化到資料庫前執行
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}