package com.example.techiedating.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class MessageController {

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> postMessage(@PathVariable String id, @RequestBody Map<String, String> body) {
        String messageId = UUID.randomUUID().toString();
        return ResponseEntity.status(201).body(Map.of("messageId", messageId, "sentAt", java.time.Instant.now().toString()));
    }
}
