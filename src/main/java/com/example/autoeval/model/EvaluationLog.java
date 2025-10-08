package com.example.autoeval.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationLog {

    private String logId;
    private String submissionId;
    private String assignmentId;
    private String studentId;
    private String testName;
    private String result;
    private String timestamp;

    @DynamoDbPartitionKey
    public String getLogId() {
        return logId;
    }
}
