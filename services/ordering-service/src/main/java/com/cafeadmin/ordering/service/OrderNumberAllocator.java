package com.cafeadmin.ordering.service;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderNumberAllocator {

    private final JdbcTemplate jdbcTemplate;

    public OrderNumberAllocator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public long next(UUID restaurantId) {
        Long allocated = jdbcTemplate.queryForObject("""
                insert into ordering.order_number_allocations (restaurant_id, next_number)
                values (?, 2)
                on conflict (restaurant_id)
                    do update set next_number = ordering.order_number_allocations.next_number + 1
                returning next_number - 1
                """, Long.class, restaurantId);
        return allocated == null ? 1L : allocated;
    }
}
