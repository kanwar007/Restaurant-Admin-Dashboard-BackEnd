package com.cafeadmin.floor.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "dining_tables", schema = "floor")
public class DiningTable {

    @Id
    private UUID id;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    private String number;

    private int capacity;

    private String status;

    @Column(name = "current_order_id")
    private UUID currentOrderId;

    @Column(name = "current_order_display")
    private String currentOrderDisplay;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Version
    private int version;

    protected DiningTable() {
    }

    public DiningTable(UUID restaurantId, String number, int capacity) {
        this.id = UUID.randomUUID();
        this.restaurantId = restaurantId;
        this.number = number;
        this.capacity = capacity;
        this.status = "vacant";
    }

    public UUID getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrentOrderDisplay() {
        return currentOrderDisplay;
    }

    public void update(String number, Integer capacity, String status) {
        if (number != null && !number.isBlank()) {
            this.number = number;
        }
        if (capacity != null) {
            this.capacity = capacity;
        }
        if (status != null) {
            this.status = status;
            if ("vacant".equals(status)) {
                this.currentOrderId = null;
                this.currentOrderDisplay = null;
            }
        }
        this.updatedAt = Instant.now();
    }

    public void occupy(UUID orderId, String orderDisplay) {
        this.status = "occupied";
        this.currentOrderId = orderId;
        this.currentOrderDisplay = orderDisplay;
        this.updatedAt = Instant.now();
    }

    public void release() {
        this.status = "vacant";
        this.currentOrderId = null;
        this.currentOrderDisplay = null;
        this.updatedAt = Instant.now();
    }
}
