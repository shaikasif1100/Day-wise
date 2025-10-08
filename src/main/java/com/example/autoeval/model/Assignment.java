package com.example.autoeval.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
    private String assignmentId;
    private String title;
    private String description;
    private int totalTestCases;
}
