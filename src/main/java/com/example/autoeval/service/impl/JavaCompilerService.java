package com.example.autoeval.service.impl;

import com.example.autoeval.model.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.File;
import java.nio.file.*;
import java.util.*;

@Component
public class JavaCompilerService {

    private static final Logger log = LoggerFactory.getLogger(JavaCompilerService.class);

    public Path compile(Submission submission) {
        try {
            Path workDir = Files.createTempDirectory("eval_" + submission.getSubmissionId());
            log.info("Created temp work directory: {}", workDir);

            // Copy student's code
            Path studentFile = Path.of(submission.getStoredFilePath());
            Path targetFile = workDir.resolve(studentFile.getFileName());
            Files.copy(studentFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

            // Locate test files for this assignment
            Path testDir = Paths.get("evaluation/testcases", submission.getAssignmentId());
            if (!Files.exists(testDir)) {
                throw new RuntimeException("Test cases not found for assignment: " + submission.getAssignmentId());
            }

            // Add all .java files (student + test)
            List<File> filesToCompile = new ArrayList<>();
            Files.list(testDir).filter(f -> f.toString().endsWith(".java")).forEach(f -> filesToCompile.add(f.toFile()));
            filesToCompile.add(targetFile.toFile());

            // Compile
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null)
                throw new RuntimeException("Java compiler not available. Use JDK, not JRE.");

            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(filesToCompile);

            List<String> options = Arrays.asList("-d", workDir.toString());
            boolean success = compiler.getTask(null, fileManager, null, options, null, units).call();
            fileManager.close();

            if (!success) throw new RuntimeException("Compilation failed for student file.");

            log.info("Compilation successful for submission {}", submission.getSubmissionId());
            return workDir;

        } catch (Exception e) {
            log.error("Compilation error: {}", e.getMessage());
            throw new RuntimeException("Compilation failed: " + e.getMessage(), e);
        }
    }
}
