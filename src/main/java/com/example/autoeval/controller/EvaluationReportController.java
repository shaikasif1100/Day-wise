package com.example.autoeval.controller;

import com.example.autoeval.service.EvaluationReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public class EvaluationReportController {

    private final EvaluationReportService reportService;

    public EvaluationReportController(EvaluationReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentSummary(@PathVariable String studentId) {
        return ResponseEntity.ok(reportService.getStudentSummary(studentId));
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getAssignmentSummary(@PathVariable String assignmentId) {
        return ResponseEntity.ok(reportService.getAssignmentSummary(assignmentId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        return ResponseEntity.ok(reportService.getLeaderboard());
    }

    @GetMapping("/overall")
    public ResponseEntity<?> getOverallStats() {
        return ResponseEntity.ok(reportService.getOverallStatistics());
    }
}
