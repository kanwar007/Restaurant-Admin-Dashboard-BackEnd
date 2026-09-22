package com.cafeadmin.common.reset;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Development-only endpoint that re-runs the seed function owned by the service schema.
 * Disable it in production with app.reset.enabled=false.
 */
@RestController
@ConditionalOnProperty(name = "app.reset.enabled", havingValue = "true", matchIfMissing = true)
public class ResetController {

    private static final Pattern FUNCTION_NAME = Pattern.compile("[a-z_]+\\.[a-z_]+");

    private final JdbcTemplate jdbcTemplate;
    private final String seedFunction;

    public ResetController(JdbcTemplate jdbcTemplate, @Value("${app.seed-function}") String seedFunction) {
        if (!FUNCTION_NAME.matcher(seedFunction).matches()) {
            throw new IllegalArgumentException("Invalid seed function name: " + seedFunction);
        }
        this.jdbcTemplate = jdbcTemplate;
        this.seedFunction = seedFunction;
    }

    @PostMapping("/internal/reset")
    public Map<String, String> reset() {
        jdbcTemplate.execute("SELECT " + seedFunction + "()");
        return Map.of("status", "reset");
    }
}
