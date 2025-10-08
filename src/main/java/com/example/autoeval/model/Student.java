package com.example.autoeval.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    private String studentId;
    private String name;
    private String email;
}
