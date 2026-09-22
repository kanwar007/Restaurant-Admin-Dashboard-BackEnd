package com.cafeadmin.reporting.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Read-only fan-out to the services that own the dashboard source data. */
@Component
public class UpstreamClients {

    private static final Logger log = LoggerFactory.getLogger(UpstreamClients.class);

    private final RestClient ordering;
    private final RestClient floor;
    private final RestClient billing;

    public UpstreamClients(RestClient.Builder builder, @Value("${app.ordering-url}") String orderingUrl,
            @Value("${app.floor-url}") String floorUrl, @Value("${app.billing-url}") String billingUrl) {
        this.ordering = builder.clone().baseUrl(orderingUrl).build();
        this.floor = builder.clone().baseUrl(floorUrl).build();
        this.billing = builder.clone().baseUrl(billingUrl).build();
    }

    public record LatestOrder(String id, String orderNo, String table, int items, String placedAgo, String status) {
    }

    public record LiveStats(int totalOrdersToday, int pendingKot, int activeOrders, List<LatestOrder> latestOrders) {
    }

    public record TableView(String id, String number, int capacity, String status, String currentOrder) {
    }

    public LiveStats orderStats() {
        try {
            LiveStats stats = ordering.get().uri("/internal/orders/stats").retrieve().body(LiveStats.class);
            return stats == null ? emptyStats() : stats;
        } catch (RestClientException exception) {
            log.warn("ordering stats unavailable: {}", exception.getMessage());
            return emptyStats();
        }
    }

    public List<TableView> tables() {
        try {
            List<TableView> tables = floor.get().uri("/api/tables").retrieve()
                    .body(new ParameterizedTypeReference<List<TableView>>() {
                    });
            return tables == null ? List.of() : tables;
        } catch (RestClientException exception) {
            log.warn("floor tables unavailable: {}", exception.getMessage());
            return List.of();
        }
    }

    public BigDecimal revenueToday() {
        try {
            Map<String, Object> body = billing.get().uri("/internal/revenue/today").retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Object revenue = body == null ? null : body.get("revenue");
            return revenue == null ? BigDecimal.ZERO : new BigDecimal(revenue.toString());
        } catch (RestClientException exception) {
            log.warn("billing revenue unavailable: {}", exception.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private static LiveStats emptyStats() {
        return new LiveStats(0, 0, 0, List.of());
    }
}
