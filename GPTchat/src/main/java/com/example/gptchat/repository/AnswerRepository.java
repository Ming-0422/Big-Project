package com.example.gptchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gptchat.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
} 