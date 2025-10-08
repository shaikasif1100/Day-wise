Feature: Automated Evaluation Workflow

  Scenario: Student uploads a valid Java file and gets a PASS result
    Given a student with ID "S001" and assignment "A001"
    When the student uploads a valid Java file
    Then the evaluation should complete successfully
    And the result should have a status "PASS"

  Scenario: Student uploads an invalid Java file and gets a FAIL result
    Given a student with ID "S002" and assignment "A001"
    When the student uploads a Java file with compilation error
    Then the evaluation should fail
    And an error log should be saved in DynamoDB
