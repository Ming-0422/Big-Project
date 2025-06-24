package com.example.gptchat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gptchat.entity.Video;

public interface VideoRepository extends JpaRepository<Video, Long> {
    
    // 根據主題查找影片
    List<Video> findBySubjectOrderByCreatedAtDesc(String subject);
    
    // 查找某個影片關聯的所有對話 session
    @Query("SELECT v FROM Video v JOIN FETCH v.chatSessions WHERE v.id = :videoId")
    Video findByIdWithChatSessions(@Param("videoId") Long videoId);
    
    // 根據標題模糊搜尋
    List<Video> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);
} 