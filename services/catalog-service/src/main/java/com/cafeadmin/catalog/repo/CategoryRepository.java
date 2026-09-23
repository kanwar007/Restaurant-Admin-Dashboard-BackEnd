package com.cafeadmin.catalog.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cafeadmin.catalog.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByRestaurantIdAndActiveTrueOrderBySortOrderAscNameAsc(UUID restaurantId);

    Optional<Category> findByRestaurantIdAndNameIgnoreCase(UUID restaurantId, String name);
}
