package com.example.autoeval.repository;

import com.example.autoeval.model.EvaluationErrorLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EvaluationErrorLogRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    private DynamoDbTable<EvaluationErrorLog> getTable() {
        return enhancedClient.table("EvaluationErrorLog", TableSchema.fromBean(EvaluationErrorLog.class));
    }

    public void saveError(EvaluationErrorLog errorLog) {
        getTable().putItem(errorLog);
    }

    public List<EvaluationErrorLog> findByStudentId(String studentId) {
        List<EvaluationErrorLog> errors = new ArrayList<>();
        getTable().scan().items().forEach(errors::add);
        return errors.stream()
                .filter(e -> e.getStudentId() != null && e.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    public void deleteByStudentId(String studentId) {
        try {
            DynamoDbTable<EvaluationErrorLog> table = getTable();
            List<EvaluationErrorLog> allErrors = new ArrayList<>();
            table.scan().items().forEach(allErrors::add);
            List<EvaluationErrorLog> toDelete = allErrors.stream()
                    .filter(e -> e.getStudentId() != null && e.getStudentId().equals(studentId))
                    .collect(Collectors.toList());
            for (EvaluationErrorLog err : toDelete) {
                table.deleteItem(r -> r.key(k -> k.partitionValue(err.getLogId())));
            }
            System.out.println("Deleted all error logs for studentId: " + studentId);
        } catch (Exception e) {
            System.err.println("Error deleting error logs for studentId " + studentId + ": " + e.getMessage());
        }
    }
}
