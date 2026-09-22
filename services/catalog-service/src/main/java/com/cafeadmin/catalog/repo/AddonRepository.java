package com.cafeadmin.catalog.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cafeadmin.catalog.domain.Addon;

public interface AddonRepository extends JpaRepository<Addon, UUID> {

    @Query("""
            select addon from Addon addon
            where addon.restaurantId = :restaurantId
              and addon.active = true
              and (cast(:search as string) is null
                   or lower(addon.name) like lower(concat('%', cast(:search as string), '%')))
            order by addon.name asc
            """)
    List<Addon> search(@Param("restaurantId") UUID restaurantId, @Param("search") String search);

    Optional<Addon> findByRestaurantIdAndNameIgnoreCase(UUID restaurantId, String name);
}
