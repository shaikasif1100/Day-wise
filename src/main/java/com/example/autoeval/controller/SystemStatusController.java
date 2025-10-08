package com.example.autoeval.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SystemStatusController {

    @GetMapping("/api/system/status")
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Auto Evaluation System is Up and Running");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "System operational. DynamoDB connection stable.");
        return response;
    }
}
