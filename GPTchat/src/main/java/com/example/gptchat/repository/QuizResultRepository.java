package com.example.gptchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gptchat.entity.QuizResult;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
} 