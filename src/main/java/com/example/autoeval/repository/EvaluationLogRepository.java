package com.example.autoeval.repository;

import com.example.autoeval.model.EvaluationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EvaluationLogRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    private DynamoDbTable<EvaluationLog> getTable() {
        return enhancedClient.table("EvaluationLog", TableSchema.fromBean(EvaluationLog.class));
    }

    public void saveLog(EvaluationLog logEntry) {
        try {
            getTable().putItem(logEntry);
        } catch (DynamoDbException e) {
            System.err.println("Failed to save log: " + e.getMessage());
        }
    }

    public List<EvaluationLog> findByStudentId(String studentId) {
        List<EvaluationLog> logs = new ArrayList<>();
        try {
            getTable().scan().items().forEach(logs::add);
            return logs.stream()
                    .filter(l -> studentId.equals(l.getStudentId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching logs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void deleteByStudentId(String studentId) {
        try {
            DynamoDbTable<EvaluationLog> table = getTable();
            List<EvaluationLog> toDelete = findByStudentId(studentId);
            for (EvaluationLog log : toDelete) {
                table.deleteItem(Key.builder().partitionValue(log.getLogId()).build());
            }
            System.out.println("Deleted logs for studentId: " + studentId);
        } catch (Exception e) {
            System.err.println("Error deleting logs for studentId " + studentId + ": " + e.getMessage());
        }
    }
}
