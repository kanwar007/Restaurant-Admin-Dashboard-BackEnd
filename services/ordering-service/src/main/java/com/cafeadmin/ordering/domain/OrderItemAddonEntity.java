package com.cafeadmin.ordering.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_item_addons", schema = "ordering")
public class OrderItemAddonEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItemEntity orderItem;

    @Column(name = "addon_id")
    private UUID addonId;

    @Column(name = "addon_name_snapshot")
    private String addonName;

    @Column(name = "unit_price_snapshot")
    private BigDecimal unitPrice;

    private int quantity;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    protected OrderItemAddonEntity() {
    }

    public OrderItemAddonEntity(UUID addonId, String addonName, BigDecimal unitPrice, int quantity) {
        this.id = UUID.randomUUID();
        this.addonId = addonId;
        this.addonName = addonName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getAddonName() {
        return addonName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    void attachTo(OrderItemEntity orderItem) {
        this.orderItem = orderItem;
    }
}
