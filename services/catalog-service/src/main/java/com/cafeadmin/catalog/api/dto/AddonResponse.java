package com.cafeadmin.catalog.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record AddonResponse(String id, String name, BigDecimal price, List<String> linkedDishes) {
}
