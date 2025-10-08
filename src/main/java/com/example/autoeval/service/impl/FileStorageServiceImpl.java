package com.example.autoeval.service.impl;

import com.example.autoeval.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    @Value("${app.uploads-dir}")
    private String baseUploadDir;

    @Override
    public Path save(String studentId, String assignmentId, MultipartFile file) {
        try {
            String today = LocalDate.now().toString();
            Path uploadDir = Paths.get(baseUploadDir, today);
            Files.createDirectories(uploadDir);

            String fileName = studentId + "_" + assignmentId + "_" + file.getOriginalFilename();
            Path destination = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved at {}", destination);
            return destination;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }


    @Override
    public Path saveToCustomFolder(String folderName, String originalFileName, MultipartFile file) {
        try {
            String today = LocalDate.now().toString();
            Path submissionDir = Paths.get(baseUploadDir, today, folderName);
            Files.createDirectories(submissionDir);

            Path destination = submissionDir.resolve(originalFileName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved (original name preserved): {}", destination);
            return destination;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file to custom folder: " + e.getMessage(), e);
        }
    }
}
