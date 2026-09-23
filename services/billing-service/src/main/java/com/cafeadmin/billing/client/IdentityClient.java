package com.cafeadmin.billing.client;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class IdentityClient {

    private static final Restaurant FALLBACK = new Restaurant("Café Admin", "Restaurant Management", null, null, null,
            new BigDecimal("0.0500"), "INR", "Asia/Kolkata");

    private final RestClient restClient;

    public IdentityClient(RestClient.Builder builder, @Value("${app.identity-url}") String identityUrl) {
        this.restClient = builder.baseUrl(identityUrl).build();
    }

    public record Restaurant(String name, String tagline, String gstin, String address, String phone,
            BigDecimal gstRate, String currency, String timezone) {

        public Map<String, Object> asBillHeader() {
            return Map.of("name", name, "tagline", tagline == null ? "" : tagline, "address",
                    address == null ? "" : address, "phone", phone == null ? "" : phone, "gstin",
                    gstin == null ? "" : gstin);
        }
    }

    public Restaurant restaurant() {
        try {
            Restaurant found = restClient.get().uri("/internal/restaurant").retrieve().body(Restaurant.class);
            return found == null ? FALLBACK : found;
        } catch (RestClientException exception) {
            return FALLBACK;
        }
    }
}
