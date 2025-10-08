package com.example.autoeval.service.impl;

import com.example.autoeval.model.Submission;
import com.example.autoeval.service.EvaluationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EvaluationEngineStub implements EvaluationEngine {

    private static final Logger log = LoggerFactory.getLogger(EvaluationEngineStub.class);

    @Override
    public void evaluate(Submission submission) {
        log.info("Starting evaluation (stub) for Submission ID: {}", submission.getSubmissionId());
        log.info("Student: {}, Assignment: {}", submission.getStudentId(), submission.getAssignmentId());
        log.info("File path: {}", submission.getStoredFilePath());
        log.info("Evaluation simulated successfully (Day 4 will implement actual test execution).");
    }
}
