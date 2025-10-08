package com.example.autoeval.service.impl;

import com.example.autoeval.model.EvaluationLog;
import com.example.autoeval.model.EvaluationResult;
import com.example.autoeval.model.EvaluationErrorLog;
import com.example.autoeval.model.Submission;
import com.example.autoeval.repository.EvaluationLogRepository;
import com.example.autoeval.repository.EvaluationResultRepository;
import com.example.autoeval.repository.EvaluationErrorLogRepository;
import com.example.autoeval.service.EvaluationEngine;
import com.example.autoeval.service.LeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Primary
@Service
public class EvaluationEngineImpl implements EvaluationEngine {

    private static final Logger log = LoggerFactory.getLogger(EvaluationEngineImpl.class);

    private final JavaCompilerService compilerService;
    private final JUnitTestRunnerService testRunnerService;
    private final EvaluationResultRepository resultRepository;
    private final EvaluationLogRepository logRepository;
    private final EvaluationErrorLogRepository errorLogRepository;
    private final LeaderboardService leaderboardService;

    public EvaluationEngineImpl(JavaCompilerService compilerService,
                                JUnitTestRunnerService testRunnerService,
                                EvaluationResultRepository resultRepository,
                                EvaluationLogRepository logRepository,
                                EvaluationErrorLogRepository errorLogRepository,
                                LeaderboardService leaderboardService) {
        this.compilerService = compilerService;
        this.testRunnerService = testRunnerService;
        this.resultRepository = resultRepository;
        this.logRepository = logRepository;
        this.errorLogRepository = errorLogRepository;
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void evaluate(Submission submission) {
        log.info("Starting evaluation for Submission ID: {}", submission.getSubmissionId());

        try {
            Path workDir = compilerService.compile(submission);
            log.info("Compilation completed successfully for Submission ID: {}", submission.getSubmissionId());

            List<String> testLogs = testRunnerService.runTests(workDir, submission.getAssignmentId());

            // Filter only valid test logs (ignore summary/unknown)
            List<String> validLogs = testLogs.stream()
                    .filter(t -> t.contains(" - PASSED") || t.contains(" - FAILED"))
                    .toList();

            int passed = (int) validLogs.stream().filter(t -> t.contains(" - PASSED")).count();
            int total = (int) validLogs.stream().filter(t -> t.contains(" - PASSED") || t.contains(" - FAILED")).count();

            double score = (total > 0) ? (passed * 100.0 / total) : 0.0;

            String finalStatus = (total > 0 && passed == total) ? "PASS" : "FAIL";

            EvaluationResult result = EvaluationResult.builder()
                    .submissionId(submission.getSubmissionId())
                    .assignmentId(submission.getAssignmentId())
                    .studentId(submission.getStudentId())
                    .score(score)
                    .status(finalStatus)
                    .timestamp(Instant.now().toString())
                    .build();

            resultRepository.saveResult(result);
            log.info("Saved evaluation result for {} -> Score: {}%", submission.getStudentId(), score);

            leaderboardService.updateScore(submission.getStudentId(), score);
            log.info("Leaderboard updated -> Student: {}, Score: {}", submission.getStudentId(), score);

            // Save only valid test logs (ignore UNKNOWN or summary lines)
            for (String testOutcome : validLogs) {
                String[] parts = testOutcome.split(" - ");
                String testName = parts.length > 0 ? parts[0] : "UnknownTest";
                String outcome = parts.length > 1 ? parts[1] : "UNKNOWN";

                EvaluationLog logEntry = EvaluationLog.builder()
                        .logId(UUID.randomUUID().toString())
                        .submissionId(submission.getSubmissionId())
                        .assignmentId(submission.getAssignmentId())
                        .studentId(submission.getStudentId())
                        .testName(testName)
                        .result(outcome)
                        .timestamp(Instant.now().toString())
                        .build();

                logRepository.saveLog(logEntry);
                log.info("Log saved -> {} : {}", testName, outcome);
            }

            log.info("Evaluation Completed -> Student: {}, Passed: {}/{} ({}%) [{}]",
                    submission.getStudentId(), passed, total, score, finalStatus);

        } catch (Exception e) {
            log.error("Evaluation failed for submission {}: {}", submission.getSubmissionId(), e.getMessage());

            EvaluationLog errorLog = EvaluationLog.builder()
                    .logId(UUID.randomUUID().toString())
                    .submissionId(submission.getSubmissionId())
                    .assignmentId(submission.getAssignmentId())
                    .studentId(submission.getStudentId())
                    .testName("SYSTEM_ERROR")
                    .result(e.getMessage())
                    .timestamp(Instant.now().toString())
                    .build();

            try {
                logRepository.saveLog(errorLog);
                log.error("Error log saved to DynamoDB for submission {}", submission.getSubmissionId());
            } catch (Exception logEx) {
                log.error("Failed to save EvaluationLog entry: {}", logEx.getMessage());
            }

            String errorType = detectErrorType(e);

            EvaluationErrorLog unstructuredError = EvaluationErrorLog.builder()
                    .logId(UUID.randomUUID().toString())
                    .submissionId(submission.getSubmissionId())
                    .studentId(submission.getStudentId())
                    .assignmentId(submission.getAssignmentId())
                    .errorType(errorType)
                    .errorMessage(e.getMessage())
                    .errorDetails(getStackTrace(e))
                    .timestamp(Instant.now().toString())
                    .build();

            try {
                errorLogRepository.saveError(unstructuredError);
                log.error("Unstructured error log saved to DynamoDB for submission {}", submission.getSubmissionId());
            } catch (Exception repoEx) {
                log.error("Failed to save unstructured error log: {}", repoEx.getMessage());
            }
        }
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("  at ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    private String detectErrorType(Exception e) {
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (message.contains("compilation")) return "COMPILATION_ERROR";
        if (message.contains("test") || message.contains("assert")) return "TEST_FAILURE";
        if (message.contains("runtime")) return "RUNTIME_ERROR";
        return "SYSTEM_ERROR";
    }
}
