package com.example.autoeval.service;

import java.util.List;
import java.util.Map;

public interface LeaderboardService {

    //Add or update score in cache
    void updateScore(String studentId, double score);

    //Get all current scores
    Map<String, Double> getAllScores();

    //Get top N students
    List<Map.Entry<String, Double>> getTopStudents(int limit);
}
