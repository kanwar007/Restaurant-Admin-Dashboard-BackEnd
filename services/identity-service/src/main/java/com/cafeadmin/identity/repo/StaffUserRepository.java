package com.cafeadmin.identity.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cafeadmin.identity.domain.StaffUser;

public interface StaffUserRepository extends JpaRepository<StaffUser, UUID> {

    Optional<StaffUser> findByUsernameAndActiveTrue(String username);

    List<StaffUser> findByRestaurantIdOrderByUsername(UUID restaurantId);
}
