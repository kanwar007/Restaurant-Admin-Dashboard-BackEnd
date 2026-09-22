package com.cafeadmin.catalog.api.dto;

import java.math.BigDecimal;

public record MenuItemResponse(String id, String name, String category, BigDecimal price, boolean available,
                               long addons) {
}
