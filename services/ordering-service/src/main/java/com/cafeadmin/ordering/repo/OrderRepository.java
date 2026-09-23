package com.cafeadmin.ordering.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cafeadmin.ordering.domain.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("""
            select o from OrderEntity o
            where o.restaurantId = :restaurantId
              and o.status in :statuses
            order by o.placedAt desc
            """)
    List<OrderEntity> findByStatuses(@Param("restaurantId") UUID restaurantId,
            @Param("statuses") Collection<String> statuses);

    Optional<OrderEntity> findByRestaurantIdAndOrderNumber(UUID restaurantId, long orderNumber);
}
