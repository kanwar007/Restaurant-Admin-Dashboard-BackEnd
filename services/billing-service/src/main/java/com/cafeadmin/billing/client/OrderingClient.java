package com.cafeadmin.billing.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OrderingClient {

    private final RestClient restClient;

    public OrderingClient(RestClient.Builder builder, @Value("${app.ordering-url}") String orderingUrl) {
        this.restClient = builder.baseUrl(orderingUrl).build();
    }

    public record OrderLine(String name, int quantity, List<String> addons, BigDecimal unitPrice, BigDecimal amount) {
    }

    public record OrderDetail(String id, String orderNo, String table, String date, String time, String status,
            String source, String notes, String customerName, List<OrderLine> lines, BigDecimal subtotal,
            int itemCount) {
    }

    public Optional<OrderDetail> findByOrderNo(String orderNo) {
        return restClient.get()
                .uri("/internal/orders/by-number/{orderNo}", orderNo.replace("#", ""))
                .exchange((request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        return Optional.<OrderDetail>empty();
                    }
                    return Optional.ofNullable(response.bodyTo(OrderDetail.class));
                });
    }

    public List<OrderDetail> closedOrders() {
        try {
            List<OrderDetail> found = restClient.get()
                    .uri("/internal/orders/closed")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<OrderDetail>>() {
                    });
            return found == null ? List.of() : found;
        } catch (RestClientException exception) {
            return List.of();
        }
    }
}
