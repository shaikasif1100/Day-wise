package com.example.autoeval.service.impl;

import com.example.autoeval.service.LeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardServiceImpl.class);

    // ⚡ Thread-safe in-memory cache
    private final Map<String, Double> studentScores = new ConcurrentHashMap<>();

    @Override
    public void updateScore(String studentId, double score) {
        studentScores.put(studentId, score);
        log.info("Leaderboard updated: {} → {}%", studentId, score);
    }

    @Override
    public Map<String, Double> getAllScores() {
        return Collections.unmodifiableMap(studentScores);
    }

    @Override
    public List<Map.Entry<String, Double>> getTopStudents(int limit) {
        return studentScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }
}
