package com.example.gptchat.entity; // 注意這裡的包名是小寫的 entity

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
// 如果您想在 Member 實體中添加 One-to-Many 關係來查看該會員的所有會話或消息，可以引入 List
// import java.util.List;

@Entity
@Table(name = "members")
public class Member {

    @Id
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String username;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password; 

    @Column(nullable = false)
    private Long coin = 0L;

    @Column(name = "current_theme", nullable = false)
    private String currentTheme = "default";

    // 如果需要，可以在這裡添加與 ChatSession 和 ChatMessage 的 One-to-Many 關係
    // @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ChatSession> sessions;

    // @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ChatMessage> messages;

    public Member() {
    }

    public Member(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getCoin() {
        return coin;
    }

    public void setCoin(Long coin) {
        this.coin = coin;
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(String currentTheme) {
        this.currentTheme = currentTheme;
    }
}