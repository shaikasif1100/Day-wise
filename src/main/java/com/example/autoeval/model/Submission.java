package com.example.autoeval.model;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    private String submissionId;
    private String studentId;
    private String assignmentId;
    private String originalFileName;
    private String storedFilePath;
    private Instant uploadedAt;
}
