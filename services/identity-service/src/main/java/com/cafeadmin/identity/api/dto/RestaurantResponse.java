package com.cafeadmin.identity.api.dto;

import java.math.BigDecimal;

import com.cafeadmin.identity.domain.Restaurant;

public record RestaurantResponse(String name, String tagline, String gstin, String address, String phone,
                                 BigDecimal gstRate, String currency, String timezone) {

    public static RestaurantResponse of(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.getName(), restaurant.getTagline(), restaurant.getGstin(),
                restaurant.getAddress(), restaurant.getPhone(), restaurant.getTaxRate(), restaurant.getCurrency(),
                restaurant.getTimezone());
    }
}
