package com.cafeadmin.identity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafeadmin.common.web.ApiException;
import com.cafeadmin.identity.api.dto.RestaurantResponse;
import com.cafeadmin.identity.api.dto.UserResponse;
import com.cafeadmin.identity.domain.Restaurant;
import com.cafeadmin.identity.repo.RestaurantRepository;
import com.cafeadmin.identity.repo.StaffUserRepository;

@Service
public class ProfileService {

    private final RestaurantRepository restaurants;
    private final StaffUserRepository users;
    private final String restaurantCode;

    public ProfileService(RestaurantRepository restaurants, StaffUserRepository users,
                          @Value("${app.restaurant-code:CAFE01}") String restaurantCode) {
        this.restaurants = restaurants;
        this.users = users;
        this.restaurantCode = restaurantCode;
    }

    @Transactional(readOnly = true)
    public RestaurantResponse restaurant() {
        return RestaurantResponse.of(currentRestaurant());
    }

    @Transactional(readOnly = true)
    public UserResponse defaultUser() {
        return users.findByRestaurantIdOrderByUsername(currentRestaurant().getId()).stream()
                .findFirst()
                .map(UserResponse::of)
                .orElseThrow(() -> ApiException.notFound("No staff users configured"));
    }

    private Restaurant currentRestaurant() {
        return restaurants.findByCode(restaurantCode)
                .orElseThrow(() -> ApiException.notFound("Restaurant " + restaurantCode + " is not configured"));
    }
}
