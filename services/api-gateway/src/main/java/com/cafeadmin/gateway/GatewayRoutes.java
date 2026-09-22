package com.cafeadmin.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Upstream service locations, configured per environment (compose, AKS, local). */
@ConfigurationProperties(prefix = "app.services")
public record GatewayRoutes(String identity, String catalog, String floor, String ordering, String billing,
        String reporting) {
}
