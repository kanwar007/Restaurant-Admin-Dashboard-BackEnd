package com.cafeadmin.common.correlation;

import java.util.UUID;

import org.slf4j.MDC;

public final class CorrelationId {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static String resolve(String incoming) {
        return incoming == null || incoming.isBlank() ? UUID.randomUUID().toString() : incoming;
    }
}
