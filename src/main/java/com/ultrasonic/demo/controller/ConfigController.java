package com.ultrasonic.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @GetMapping("/ice-servers")
    public ResponseEntity<List<Map<String, Object>>> getIceServers() {
        // In a real production app, this would dynamically generate short-lived TURN credentials
        // (e.g. via Twilio or a Coturn server REST API)
        Map<String, Object> stunServer = new HashMap<>();
        stunServer.put("urls", "stun:stun.l.google.com:19302");
        
        return ResponseEntity.ok(Arrays.asList(stunServer));
    }
}
