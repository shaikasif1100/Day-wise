package com.example.autoeval.repository;

import com.example.autoeval.model.EvaluationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EvaluationResultRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    private DynamoDbTable<EvaluationResult> getTable() {
        return enhancedClient.table("EvaluationResult", TableSchema.fromBean(EvaluationResult.class));
    }

    public void saveResult(EvaluationResult result) {
        try {
            getTable().putItem(result);
        } catch (DynamoDbException e) {
            System.err.println("Failed to save EvaluationResult: " + e.getMessage());
        }
    }

    public List<EvaluationResult> findByStudentId(String studentId) {
        try {
            List<EvaluationResult> all = findAll();
            return all.stream()
                    .filter(r -> studentId.equals(r.getStudentId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching results by studentId: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<EvaluationResult> findByAssignmentId(String assignmentId) {
        try {
            List<EvaluationResult> all = findAll();
            return all.stream()
                    .filter(r -> assignmentId.equals(r.getAssignmentId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching results by assignmentId: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<EvaluationResult> findAll() {
        List<EvaluationResult> results = new ArrayList<>();
        try {
            DynamoDbTable<EvaluationResult> table = getTable();
            table.scan().items().forEach(results::add);
        } catch (DynamoDbException e) {
            System.err.println("Error fetching all results: " + e.getMessage());
        }
        return results;
    }

    public void deleteByStudentId(String studentId) {
        try {
            DynamoDbTable<EvaluationResult> table = getTable();
            List<EvaluationResult> toDelete = findByStudentId(studentId);
            for (EvaluationResult item : toDelete) {
                table.deleteItem(Key.builder()
                        .partitionValue(item.getSubmissionId())
                        .sortValue(item.getAssignmentId())
                        .build());
            }
            System.out.println("Deleted results for studentId: " + studentId);
        } catch (Exception e) {
            System.err.println("Error deleting results for studentId " + studentId + ": " + e.getMessage());
        }
    }
}
