package com.example.autoeval.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResult {

    private String submissionId;
    private String studentId;
    private String assignmentId;
    private double score;
    private String status;
    private String timestamp;

    @DynamoDbPartitionKey
    public String getSubmissionId() {
        return submissionId;
    }

    @DynamoDbSortKey
    public String getAssignmentId() {
        return assignmentId;
    }
}

