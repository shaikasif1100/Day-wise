package com.example.autoeval.controller;

import com.example.autoeval.dto.UploadResponse;
import com.example.autoeval.model.Submission;
import com.example.autoeval.repository.EvaluationResultRepository;
import com.example.autoeval.service.EvaluationEngine;
import com.example.autoeval.service.FileStorageService;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/assignments")
@Validated
public class AssignmentUploadController {

    private static final Logger log = LoggerFactory.getLogger(AssignmentUploadController.class);

    private final FileStorageService fileStorageService;
    private final EvaluationEngine evaluationEngine;
    private final EvaluationResultRepository resultRepository;

    public AssignmentUploadController(FileStorageService fileStorageService,
                                      EvaluationEngine evaluationEngine,
                                      EvaluationResultRepository resultRepository) {
        this.fileStorageService = fileStorageService;
        this.evaluationEngine = evaluationEngine;
        this.resultRepository = resultRepository;
    }

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<UploadResponse> uploadAssignment(
            @RequestParam @NotBlank String studentId,
            @RequestParam @NotBlank String assignmentId,
            @RequestPart("file") MultipartFile file) {

        log.info("Upload request received for Student: {}, Assignment: {}", studentId, assignmentId);

        try {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || !originalFileName.endsWith(".java")) {
                throw new IllegalArgumentException("Invalid file. Only .java files are allowed.");
            }

            String submissionFolder = studentId + "_" + assignmentId + "_" + UUID.randomUUID();
            Path savedFilePath = fileStorageService.saveToCustomFolder(submissionFolder, originalFileName, file);

            Submission submission = Submission.builder()
                    .submissionId(UUID.randomUUID().toString())
                    .studentId(studentId)
                    .assignmentId(assignmentId)
                    .originalFileName(originalFileName)
                    .storedFilePath(savedFilePath.toAbsolutePath().toString())
                    .uploadedAt(Instant.now())
                    .build();

            log.info("Triggering evaluation for Submission ID: {}", submission.getSubmissionId());
            evaluationEngine.evaluate(submission);

            UploadResponse response = UploadResponse.builder()
                    .submissionId(submission.getSubmissionId())
                    .message("Upload successful. Evaluation started for " + originalFileName)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to upload assignment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(UploadResponse.builder()
                            .message("Upload failed: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/deleteResult/{studentId}")
    public ResponseEntity<String> deleteResults(@PathVariable String studentId) {
        try {
            resultRepository.deleteByStudentId(studentId);
            return ResponseEntity.ok("Deleted results for studentId: " + studentId);
        } catch (Exception e) {
            log.error("Failed to delete data for studentId {}: {}", studentId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to delete records for studentId: " + studentId + " -> " + e.getMessage());
        }
    }

    @GetMapping("/results/{studentId}")
    public ResponseEntity<?> getResultsByStudentId(@PathVariable String studentId) {
        try {
            var results = resultRepository.findByStudentId(studentId);
            if (results.isEmpty()) {
                return ResponseEntity.status(404).body("No results found for studentId: " + studentId);
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error fetching results for studentId {}: {}", studentId, e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to fetch results: " + e.getMessage());
        }
    }

    @GetMapping("/results")
    public ResponseEntity<?> getAllResults() {
        try {
            var allResults = resultRepository.findAll();
            if (allResults.isEmpty()) {
                return ResponseEntity.status(404).body("No results found in EvaluationResult table");
            }
            return ResponseEntity.ok(allResults);
        } catch (Exception e) {
            log.error("Error fetching all results: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to fetch all results: " + e.getMessage());
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        try {
            var allResults = resultRepository.findAll();
            if (allResults.isEmpty()) {
                return ResponseEntity.status(404).body("No data found in leaderboard");
            }

            var leaderboard = allResults.stream()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .map(result -> {
                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("studentId", result.getStudentId());
                        entry.put("assignmentId", result.getAssignmentId());
                        entry.put("score", result.getScore());
                        entry.put("status", result.getStatus());
                        return entry;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            log.error("Error fetching leaderboard: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to fetch leaderboard: " + e.getMessage());
        }
    }
}
