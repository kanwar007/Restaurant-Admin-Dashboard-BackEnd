package com.cafeadmin.floor.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cafeadmin.floor.domain.DiningTable;

public interface DiningTableRepository extends JpaRepository<DiningTable, UUID> {

    List<DiningTable> findByRestaurantIdOrderByNumberAsc(UUID restaurantId);

    Optional<DiningTable> findByRestaurantIdAndNumberIgnoreCase(UUID restaurantId, String number);

    Optional<DiningTable> findByRestaurantIdAndCurrentOrderDisplay(UUID restaurantId, String currentOrderDisplay);
}
