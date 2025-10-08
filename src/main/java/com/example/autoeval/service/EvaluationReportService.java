package com.example.autoeval.service;

import java.util.Map;
import java.util.List;

public interface EvaluationReportService {

    Map<String, Object> getStudentSummary(String studentId);

    Map<String, Object> getAssignmentSummary(String assignmentId);

    List<Map<String, Object>> getLeaderboard();

    Map<String, Object> getOverallStatistics();
}
