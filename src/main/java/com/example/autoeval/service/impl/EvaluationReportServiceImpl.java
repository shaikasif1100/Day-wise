package com.example.autoeval.service.impl;

import com.example.autoeval.model.EvaluationResult;
import com.example.autoeval.repository.EvaluationResultRepository;
import com.example.autoeval.service.EvaluationReportService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluationReportServiceImpl implements EvaluationReportService {

    private final EvaluationResultRepository resultRepository;

    public EvaluationReportServiceImpl(EvaluationResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Override
    public Map<String, Object> getStudentSummary(String studentId) {
        List<EvaluationResult> results = resultRepository.findByStudentId(studentId);

        double avgScore = results.stream()
                .mapToDouble(EvaluationResult::getScore)
                .average()
                .orElse(0.0);

        long passed = results.stream()
                .filter(r -> "PASS".equalsIgnoreCase(r.getStatus()))
                .count();

        return Map.of(
                "studentId", studentId,
                "totalAssignments", results.size(),
                "passed", passed,
                "failed", results.size() - passed,
                "averageScore", avgScore,
                "results", results
        );
    }

    @Override
    public Map<String, Object> getAssignmentSummary(String assignmentId) {
        List<EvaluationResult> results = resultRepository.findByAssignmentId(assignmentId);

        double avgScore = results.stream()
                .mapToDouble(EvaluationResult::getScore)
                .average()
                .orElse(0.0);

        long passCount = results.stream()
                .filter(r -> "PASS".equalsIgnoreCase(r.getStatus()))
                .count();

        return Map.of(
                "assignmentId", assignmentId,
                "totalSubmissions", results.size(),
                "passed", passCount,
                "failed", results.size() - passCount,
                "averageScore", avgScore,
                "submissions", results
        );
    }

    @Override
    public List<Map<String, Object>> getLeaderboard() {
        List<EvaluationResult> allResults = resultRepository.findAll();

        Map<String, Double> studentAvgScores = allResults.stream()
                .collect(Collectors.groupingBy(
                        EvaluationResult::getStudentId,
                        Collectors.averagingDouble(EvaluationResult::getScore)
                ));

        return studentAvgScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("studentId", e.getKey());
                    entry.put("averageScore", e.getValue());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getOverallStatistics() {
        List<EvaluationResult> all = resultRepository.findAll();

        double avg = all.stream()
                .mapToDouble(EvaluationResult::getScore)
                .average()
                .orElse(0.0);

        long passCount = all.stream()
                .filter(r -> "PASS".equalsIgnoreCase(r.getStatus()))
                .count();

        return Map.of(
                "totalEvaluations", all.size(),
                "passed", passCount,
                "failed", all.size() - passCount,
                "averageScore", avg
        );
    }
}
