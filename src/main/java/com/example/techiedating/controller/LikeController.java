package com.example.techiedating.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {

    @PostMapping
    public ResponseEntity<?> likeUser(@RequestBody Map<String, String> body) {
        // body: {"toUserId":"..."}
        String id = UUID.randomUUID().toString();
        return ResponseEntity.status(201).body(Map.of("liked", true, "match", false, "id", id));
    }
}
