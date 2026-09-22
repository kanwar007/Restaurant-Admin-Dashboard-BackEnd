package com.cafeadmin.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Maps the public openapi.yaml contract onto the owning microservice. */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator apiRoutes(RouteLocatorBuilder builder, GatewayRoutes services) {
        return builder.routes()
                .route("identity", route -> route
                        .path("/api/auth/**", "/api/profile")
                        .uri(services.identity()))
                .route("catalog", route -> route
                        .path("/api/menu/**", "/api/menu", "/api/addons/**", "/api/addons")
                        .uri(services.catalog()))
                .route("floor", route -> route
                        .path("/api/tables/**", "/api/tables")
                        .uri(services.floor()))
                .route("ordering", route -> route
                        .path("/api/orders/**", "/api/orders", "/api/guest/orders")
                        .uri(services.ordering()))
                .route("billing", route -> route
                        .path("/api/bills/**", "/api/order-history")
                        .uri(services.billing()))
                .route("reporting", route -> route
                        .path("/api/dashboard")
                        .uri(services.reporting()))
                .build();
    }
}
