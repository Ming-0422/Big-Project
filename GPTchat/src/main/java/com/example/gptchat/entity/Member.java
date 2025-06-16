package com.example.gptchat.entity; // 注意這裡的包名是小寫的 entity

import jakarta.persistence.*;
import java.time.LocalDateTime;
// 如果您想在 Member 實體中添加 One-to-Many 關係來查看該會員的所有會話或消息，可以引入 List
// import java.util.List;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash; // 儲存密碼的雜湊值

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 如果需要，可以在這裡添加與 ChatSession 和 ChatMessage 的 One-to-Many 關係
    // @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ChatSession> sessions;

    // @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ChatMessage> messages;

    public Member() {
        this.createdAt = LocalDateTime.now();
    }

    public Member(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}