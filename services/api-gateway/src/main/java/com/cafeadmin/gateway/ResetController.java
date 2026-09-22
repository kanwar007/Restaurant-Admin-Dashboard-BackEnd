package com.cafeadmin.gateway;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Development helper that re-seeds every service. Disable with APP_RESET_ENABLED=false in production.
 */
@RestController
@ConditionalOnProperty(name = "app.reset.enabled", havingValue = "true", matchIfMissing = true)
public class ResetController {

    private final WebClient webClient;
    private final List<String> resettableServices;

    public ResetController(WebClient.Builder builder, GatewayRoutes routes) {
        this.webClient = builder.build();
        this.resettableServices = List.of(routes.identity(), routes.catalog(), routes.floor(), routes.ordering(),
                routes.billing());
    }

    @PostMapping("/api/reset")
    public Mono<Map<String, String>> reset() {
        return Flux.fromIterable(resettableServices)
                .concatMap(baseUrl -> webClient.post()
                        .uri(baseUrl + "/internal/reset")
                        .retrieve()
                        .bodyToMono(String.class))
                .then(Mono.just(Map.of("status", "reset")));
    }
}
