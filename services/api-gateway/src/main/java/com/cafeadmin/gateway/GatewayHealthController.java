package com.cafeadmin.gateway;

import java.lang.management.ManagementFactory;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayHealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0);
    }
}
