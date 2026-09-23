package com.cafeadmin.ordering.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_status_history", schema = "ordering")
public class OrderStatusHistoryEntity {

    @Id
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "from_status")
    private String fromStatus;

    @Column(name = "to_status")
    private String toStatus;

    @Column(name = "changed_by")
    private UUID changedBy;

    private String reason;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    protected OrderStatusHistoryEntity() {
    }

    public OrderStatusHistoryEntity(UUID orderId, String fromStatus, String toStatus) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }
}
