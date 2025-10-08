package com.example.autoeval.web;

import com.example.autoeval.service.EvaluationReportService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class EvaluationReportWebController {

    private final EvaluationReportService reportService;

    public EvaluationReportWebController(EvaluationReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/student/{studentId}")
    public Map<String, Object> getStudentReport(@PathVariable String studentId) {
        return reportService.getStudentSummary(studentId);
    }

    @GetMapping("/assignment/{assignmentId}")
    public Map<String, Object> getAssignmentReport(@PathVariable String assignmentId) {
        return reportService.getAssignmentSummary(assignmentId);
    }

    @GetMapping("/leaderboard")
    public List<Map<String, Object>> getLeaderboard() {
        return reportService.getLeaderboard();
    }

    @GetMapping("/overall")
    public Map<String, Object> getOverallStats() {
        return reportService.getOverallStatistics();
    }
}
