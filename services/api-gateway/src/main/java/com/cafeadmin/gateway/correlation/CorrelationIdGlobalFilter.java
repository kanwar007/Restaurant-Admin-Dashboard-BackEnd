package com.cafeadmin.gateway.correlation;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
        String correlationId = incoming == null || incoming.isBlank() ? UUID.randomUUID().toString() : incoming;

        ServerWebExchange mutated = exchange.mutate()
                .request(request -> request.header(HEADER, correlationId))
                .build();
        mutated.getResponse().beforeCommit(() -> {
            mutated.getResponse().getHeaders().set(HEADER, correlationId);
            return Mono.empty();
        });

        MDC.put(MDC_KEY, correlationId);
        try {
            log.info("{} {}", mutated.getRequest().getMethod(), mutated.getRequest().getURI().getPath());
        } finally {
            MDC.remove(MDC_KEY);
        }

        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
