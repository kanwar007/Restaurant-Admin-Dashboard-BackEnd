package com.cafeadmin.ordering.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cafeadmin.ordering.domain.OrderStatusHistoryEntity;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, UUID> {
}
