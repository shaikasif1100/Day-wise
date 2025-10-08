package com.example.autoeval.service;

import com.example.autoeval.model.Submission;

public interface EvaluationEngine {
    void evaluate(Submission submission);
}
