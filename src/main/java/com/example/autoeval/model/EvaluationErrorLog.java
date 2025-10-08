package com.example.autoeval.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationErrorLog {

    private String logId;
    private String submissionId;
    private String studentId;
    private String assignmentId;
    private String errorType;     // e.g., COMPILATION_ERROR, RUNTIME_EXCEPTION, SYSTEM_FAILURE
    private String errorMessage;  // Short summary
    private String errorDetails;  // Full stack trace or detailed message
    private String timestamp;

    @DynamoDbPartitionKey
    public String getLogId() {
        return logId;
    }
}
