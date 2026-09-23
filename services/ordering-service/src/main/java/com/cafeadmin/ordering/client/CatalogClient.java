package com.cafeadmin.ordering.client;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(RestClient.Builder builder, @Value("${app.catalog-url}") String catalogUrl) {
        this.restClient = builder.baseUrl(catalogUrl).build();
    }

    public record MenuItemView(String id, String name, String category, BigDecimal price, boolean available) {
    }

    public record AddonView(String id, String name, BigDecimal price) {
    }

    public Optional<MenuItemView> findMenuItem(String name) {
        return lookup("/internal/menu-items/by-name/{name}", name, MenuItemView.class);
    }

    public Optional<AddonView> findAddon(String name) {
        return lookup("/internal/addons/by-name/{name}", name, AddonView.class);
    }

    private <T> Optional<T> lookup(String path, String name, Class<T> type) {
        return restClient.get()
                .uri(path, name)
                .exchange((request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        return Optional.<T>empty();
                    }
                    return Optional.ofNullable(response.bodyTo(type));
                });
    }
}
