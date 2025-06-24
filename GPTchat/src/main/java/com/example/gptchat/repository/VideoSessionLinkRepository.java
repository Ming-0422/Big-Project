package com.example.gptchat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gptchat.entity.VideoSessionLink;

public interface VideoSessionLinkRepository extends JpaRepository<VideoSessionLink, Long> {
    
    // 根據影片ID查找所有關聯的對話
    List<VideoSessionLink> findByVideoIdOrderByStartTimeSecondsAsc(Long videoId);
    
    // 根據對話ID查找關聯的影片
    Optional<VideoSessionLink> findByChatSessionId(Long sessionId);
    
    // 查找特定影片和對話的關聯
    Optional<VideoSessionLink> findByVideoIdAndChatSessionId(Long videoId, Long sessionId);
    
    // 根據時間範圍查找對話
    @Query("SELECT vsl FROM VideoSessionLink vsl WHERE vsl.video.id = :videoId " +
           "AND vsl.startTimeSeconds <= :timeSeconds " +
           "AND (vsl.endTimeSeconds IS NULL OR vsl.endTimeSeconds >= :timeSeconds)")
    List<VideoSessionLink> findByVideoIdAndTimeRange(@Param("videoId") Long videoId, 
                                                     @Param("timeSeconds") Integer timeSeconds);
} 