package com.example.techiedating.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test Controller", description = "Endpoints for testing API functionality")
public class TestController {

    @GetMapping("/ping")
    @Operation(summary = "Ping endpoint", description = "Returns a simple ping response to test if the API is running")
    public String ping() {
        return "Pong! API is running.";
    }
}
