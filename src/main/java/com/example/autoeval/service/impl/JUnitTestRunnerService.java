package com.example.autoeval.service.impl;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JUnitTestRunnerService {

    public List<String> runTests(Path workDir, String assignmentId) {
        List<String> logs = new ArrayList<>();

        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();

        try (URLClassLoader testClassLoader =
                     new URLClassLoader(new URL[]{workDir.toUri().toURL()}, originalCl)) {

            // Make sure Jupiter engine is discoverable
            Thread.currentThread().setContextClassLoader(testClassLoader);

            // ✅ Find all compiled test classes (those starting with Test)
            List<String> testClasses = findTestClasses(workDir);

            if (testClasses.isEmpty()) {
                // fallback to default TestCase1
                if (Files.exists(workDir.resolve("TestCase1.class"))) {
                    testClasses = List.of("TestCase1");
                }
            }

            // ✅ Build discovery request
            LauncherDiscoveryRequestBuilder builder = LauncherDiscoveryRequestBuilder.request()
                    .filters(EngineFilter.includeEngines("junit-jupiter")); // Ensure JUnit Jupiter is used

            if (!testClasses.isEmpty()) {
                List<DiscoverySelector> selectors = testClasses.stream()
                        .map(DiscoverySelectors::selectClass)
                        .collect(Collectors.toList());
                builder.selectors(selectors);
            } else {
                builder.selectors(DiscoverySelectors.selectClasspathRoots(Set.of(workDir)));
            }

            LauncherDiscoveryRequest request = builder.build();

            Launcher launcher = LauncherFactory.create();
            TestLogListener perTest = new TestLogListener(logs);

            // ✅ Register listeners
            launcher.registerTestExecutionListeners(perTest, summaryListener);

            // ✅ Execute tests
            launcher.execute(request);

            // ✅ Collect JUnit summary results
            var summary = summaryListener.getSummary();
            long totalTests = summary.getTestsFoundCount();
            long passedTests = summary.getTestsSucceededCount();
            long failedTests = summary.getTestsFailedCount();

            // ✅ Add readable logs for EvaluationEngineImpl
            logs.add("Total Tests: " + totalTests + " : " + (failedTests == 0 ? "PASS" : "FAIL"));
            logs.add("Passed: " + passedTests + " : " + (passedTests > 0 ? "PASS" : "FAIL"));
            logs.add("Failed: " + failedTests + " : " + (failedTests > 0 ? "FAIL" : "PASS"));
            logs.add("Status: " + (failedTests == 0 ? "PASS" : "FAIL"));

            // ✅ Add synthetic failure if nothing executed
            if (totalTests == 0) {
                logs.add("NO_TESTS_EXECUTED - FAILED");
            }

            return logs;

        } catch (Exception e) {
            logs.add("SYSTEM_ERROR - FAILED - " + e.getMessage());
            e.printStackTrace();
            return logs;
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    private List<String> findTestClasses(Path root) throws Exception {
        if (!Files.exists(root)) return List.of();
        try (var stream = Files.walk(root)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".class"))
                    .map(root::relativize)
                    .map(Path::toString)
                    .filter(n -> n.matches("(?i)Test.*\\.class") || n.matches(".*Test.*\\.class"))
                    .map(n -> n.replace(".class", "").replace('/', '.').replace('\\', '.'))
                    .collect(Collectors.toList());
        }
    }

    /** ✅ Custom listener to record each test status */
    static class TestLogListener implements TestExecutionListener {
        private final List<String> logs;
        private int executed = 0;

        TestLogListener(List<String> logs) {
            this.logs = logs;
        }

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
            if (testIdentifier.isTest()) {
                executed++;
                String name = testIdentifier.getDisplayName();
                String status = switch (result.getStatus()) {
                    case SUCCESSFUL -> "PASSED";
                    case FAILED, ABORTED -> "FAILED";
                };
                String msg = result.getThrowable()
                        .map(Throwable::getMessage)
                        .orElse("");
                if (!msg.isBlank()) {
                    logs.add(name + " - " + status + " - " + msg);
                } else {
                    logs.add(name + " - " + status);
                }
            }
        }

        int getExecutedCount() {
            return executed;
        }
    }
}
