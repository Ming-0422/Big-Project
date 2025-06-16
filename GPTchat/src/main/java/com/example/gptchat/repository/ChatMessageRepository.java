package com.example.gptchat.repository;

import com.example.gptchat.entity.ChatMessage; // 注意這裡引用的是 com.example.gptchat.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 查找屬於特定會員的所有訊息，並按時間排序
    List<ChatMessage> findByMemberIdOrderByTimestampAsc(Long memberId);

    // 新增：查找屬於特定會話的所有訊息，並按時間排序
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);
}