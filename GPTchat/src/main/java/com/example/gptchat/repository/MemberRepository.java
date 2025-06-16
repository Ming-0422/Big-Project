package com.example.gptchat.repository;

import com.example.gptchat.entity.Member; // 注意這裡引用的是 com.example.gptchat.entity.Member
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 可以添加自定義查詢方法，例如根據用戶名查找會員
    Optional<Member> findByUsername(String username);
}