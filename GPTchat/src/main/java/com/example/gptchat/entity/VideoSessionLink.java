package com.example.gptchat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "video_session_links")
public class VideoSessionLink {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;
    
    @Column(name = "start_time_seconds")
    private Integer startTimeSeconds; // 對話開始時的影片時間點（秒）
    
    @Column(name = "end_time_seconds")
    private Integer endTimeSeconds; // 對話結束時的影片時間點（秒）
    
    @Column(name = "last_viewed_time_seconds")
    private Integer lastViewedTimeSeconds; // 最後觀看到的時間點（秒）
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 建構子
    public VideoSessionLink() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public VideoSessionLink(Video video, ChatSession chatSession, Integer startTimeSeconds) {
        this.video = video;
        this.chatSession = chatSession;
        this.startTimeSeconds = startTimeSeconds;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }
    
    public ChatSession getChatSession() { return chatSession; }
    public void setChatSession(ChatSession chatSession) { this.chatSession = chatSession; }
    
    public Integer getStartTimeSeconds() { return startTimeSeconds; }
    public void setStartTimeSeconds(Integer startTimeSeconds) { this.startTimeSeconds = startTimeSeconds; }
    
    public Integer getEndTimeSeconds() { return endTimeSeconds; }
    public void setEndTimeSeconds(Integer endTimeSeconds) { this.endTimeSeconds = endTimeSeconds; }
    
    public Integer getLastViewedTimeSeconds() { return lastViewedTimeSeconds; }
    public void setLastViewedTimeSeconds(Integer lastViewedTimeSeconds) { 
        this.lastViewedTimeSeconds = lastViewedTimeSeconds;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 