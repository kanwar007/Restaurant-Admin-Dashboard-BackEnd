package com.cafeadmin.common.web;

import java.lang.management.ManagementFactory;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        double uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0;
        return Map.of("status", "ok", "uptime", uptimeSeconds);
    }
}
