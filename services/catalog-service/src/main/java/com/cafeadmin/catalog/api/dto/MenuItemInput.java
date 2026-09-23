package com.cafeadmin.catalog.api.dto;

import java.math.BigDecimal;

public record MenuItemInput(String name, String category, BigDecimal price, Boolean available) {
}
