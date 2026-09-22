package com.cafeadmin.billing.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cafeadmin.billing.domain.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByRestaurantIdOrderByIssuedAtDesc(UUID restaurantId);

    Optional<Invoice> findByRestaurantIdAndOrderNo(UUID restaurantId, String orderNo);
}
