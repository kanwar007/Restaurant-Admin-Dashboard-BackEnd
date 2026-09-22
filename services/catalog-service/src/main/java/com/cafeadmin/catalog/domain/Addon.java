package com.cafeadmin.catalog.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "addons", schema = "catalog")
public class Addon {

    @Id
    private UUID id;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    private String name;

    private BigDecimal price;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    protected Addon() {
    }

    public Addon(UUID restaurantId, String name, BigDecimal price) {
        this.id = UUID.randomUUID();
        this.restaurantId = restaurantId;
        this.name = name;
        this.price = price;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public void update(String name, BigDecimal price) {
        if (name != null) {
            this.name = name;
        }
        if (price != null) {
            this.price = price;
        }
        this.updatedAt = Instant.now();
    }
}
