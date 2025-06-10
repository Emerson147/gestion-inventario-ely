package com.emersondev.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
public class DiagnosticController {

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    Map<String, Object> status = new HashMap<>();
    status.put("status", "UP");
    status.put("timestamp", LocalDateTime.now());
    status.put("memory", Runtime.getRuntime().freeMemory());

    return ResponseEntity.ok(status);
  }

  @GetMapping("/system-info")
  public ResponseEntity<Map<String, Object>> systemInfo() {
    Map<String, Object> info = new HashMap<>();
    Runtime runtime = Runtime.getRuntime();

    info.put("availableProcessors", runtime.availableProcessors());
    info.put("freeMemory", runtime.freeMemory());
    info.put("maxMemory", runtime.maxMemory());
    info.put("totalMemory", runtime.totalMemory());
    info.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
    info.put("systemLoad", ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());

    return ResponseEntity.ok(info);
  }
}
