package com.example.gptchat.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "videos")
public class Video {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "video_url", length = 500)
    private String videoUrl;
    
    @Column(name = "youtube_id", length = 100)
    private String youtubeId;

    @Column(length = 50)
    private String type = "general";
    
    @Column(length = 100)
    private String subject;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds; // 影片總長度（秒）
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // One-to-Many 關係：一個影片可以有多個對話
    @OneToMany(mappedBy = "video")
    private List<ChatSession> chatSessions;
    
    // 建構子
    public Video() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Video(String title, String description, String videoUrl, String subject) {
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.subject = subject;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    
    public String getYoutubeId() { return youtubeId; }
    public void setYoutubeId(String youtubeId) { this.youtubeId = youtubeId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<ChatSession> getChatSessions() { return chatSessions; }
    public void setChatSessions(List<ChatSession> chatSessions) { this.chatSessions = chatSessions; }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
} 