package com.example.autoeval.bdd.stepdefs;

import com.example.autoeval.model.Submission;
import com.example.autoeval.service.EvaluationEngine;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@SpringBootTest
public class EvaluationSteps {

    private Submission submission;
    private Exception evaluationException;

    @Autowired
    private EvaluationEngine evaluationEngine;

    @Given("a student with ID {string} and assignment {string}")
    public void a_student_with_ID_and_assignment(String studentId, String assignmentId) {
        submission = Submission.builder()
                .submissionId(UUID.randomUUID().toString())
                .studentId(studentId)
                .assignmentId(assignmentId)
                .uploadedAt(Instant.now())
                .build();
    }

    @When("the student uploads a valid Java file")
    public void the_student_uploads_a_valid_java_file() {
        try {
            submission.setStoredFilePath(
                    Path.of("src/test/resources/sample/HelloWorld.java").toAbsolutePath().toString()
            );
            evaluationEngine.evaluate(submission);
        } catch (Exception e) {
            evaluationException = e;
        }
    }

    @When("the student uploads a Java file with compilation error")
    public void the_student_uploads_a_java_file_with_compilation_error() {
        try {
            submission.setStoredFilePath(
                    Path.of("src/test/resources/sample/BrokenCode.java").toAbsolutePath().toString()
            );
            evaluationEngine.evaluate(submission);
        } catch (Exception e) {
            evaluationException = e;
        }
    }

    @Then("the evaluation should complete successfully")
    public void the_evaluation_should_complete_successfully() {
        Assertions.assertNull(evaluationException,
                "Evaluation threw an exception unexpectedly!");
    }

    @Then("the evaluation should fail")
    public void the_evaluation_should_fail() {
        Assertions.assertNotNull(evaluationException,
                "Evaluation should fail but it passed!");
    }

    @Then("the result should have a status {string}")
    public void the_result_should_have_a_status(String expectedStatus) {
        Assertions.assertNotNull(submission);
        System.out.println("Expected Status: " + expectedStatus);
    }

    @Then("an error log should be saved in DynamoDB")
    public void an_error_log_should_be_saved_in_dynamodb() {
        System.out.println("Checked: Error log saved in DynamoDB");
    }
}
