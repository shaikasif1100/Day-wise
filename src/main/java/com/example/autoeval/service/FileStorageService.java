package com.example.autoeval.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    Path save(String studentId, String assignmentId, MultipartFile file);

    //New method to save file inside a custom folder using its original name
    Path saveToCustomFolder(String folderName, String originalFileName, MultipartFile file);
}
