package com.example.gptchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gptchat.entity.WatchProgress;

public interface WatchProgressRepository extends JpaRepository<WatchProgress, Long> {
} 