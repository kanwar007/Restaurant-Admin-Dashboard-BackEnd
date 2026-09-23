package com.cafeadmin.ordering.client;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.cafeadmin.common.web.ApiException;

@Component
public class FloorClient {

    private static final Logger log = LoggerFactory.getLogger(FloorClient.class);

    private final RestClient restClient;

    public FloorClient(RestClient.Builder builder, @Value("${app.floor-url}") String floorUrl) {
        this.restClient = builder.baseUrl(floorUrl).build();
    }

    public void occupy(String tableNumber, UUID orderId, String orderNo) {
        try {
            restClient.post().uri("/internal/tables/occupancy")
                    .body(Map.of("table", tableNumber, "orderId", orderId.toString(), "orderNo", orderNo))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw ApiException.badRequest("Table " + tableNumber + " does not exist");
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException | HttpServerErrorException exception) {
            log.warn("floor service occupancy call failed: {}", exception.getMessage());
        }
    }

    public void release(String orderNo) {
        send("/internal/tables/release", Map.of("orderNo", orderNo));
    }

    private void send(String path, Map<String, String> body) {
        try {
            restClient.post().uri(path).body(body).retrieve().toBodilessEntity();
        } catch (RestClientException exception) {
            log.warn("floor service call {} failed: {}", path, exception.getMessage());
        }
    }
}
