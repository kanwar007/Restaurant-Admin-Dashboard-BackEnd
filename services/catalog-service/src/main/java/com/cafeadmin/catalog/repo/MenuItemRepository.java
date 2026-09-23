package com.cafeadmin.catalog.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cafeadmin.catalog.domain.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    @Query("""
            select item from MenuItem item
            join fetch item.category category
            where item.restaurantId = :restaurantId
              and item.deletedAt is null
              and (cast(:category as string) is null or lower(category.name) = lower(cast(:category as string)))
              and (cast(:search as string) is null
                   or lower(item.name) like lower(concat('%', cast(:search as string), '%')))
            order by category.sortOrder asc, item.sortOrder asc, item.name asc
            """)
    List<MenuItem> search(@Param("restaurantId") UUID restaurantId, @Param("category") String category,
                          @Param("search") String search);

    Optional<MenuItem> findByIdAndDeletedAtIsNull(UUID id);

    Optional<MenuItem> findByRestaurantIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID restaurantId, String name);
}
