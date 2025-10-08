package com.example.autoeval.controller;

import com.example.autoeval.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    //Get full leaderboard
    @GetMapping
    public ResponseEntity<Map<String, Double>> getLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getAllScores());
    }

    //Get top N students
    @GetMapping("/top/{limit}")
    public ResponseEntity<List<Map.Entry<String, Double>>> getTopStudents(@PathVariable int limit) {
        return ResponseEntity.ok(leaderboardService.getTopStudents(limit));
    }
}
