package com.example.gptchat.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.gptchat.entity.Member;
import com.example.gptchat.repository.MemberRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;

    public DataInitializer(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 檢查是否已存在 ID 為 1 的測試用戶
        if (!memberRepository.existsById(1L)) {
            Member testMember = new Member("test_user", "test@example.com", "hashed_password");
            memberRepository.save(testMember);
            System.out.println("測試用戶已創建：ID = 1, Username = test_user");
        } else {
            System.out.println("測試用戶已存在：ID = 1");
        }
    }
} 