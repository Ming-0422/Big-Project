package com.example.gptchat.repository;

import com.example.gptchat.entity.ChatSession; // 注意這裡引用的是 com.example.gptchat.entity.ChatSession
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    // 根據 Member ID 查找所有對話 session，並按創建時間降序排序
    List<ChatSession> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}