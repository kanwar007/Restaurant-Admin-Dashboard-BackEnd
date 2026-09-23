package com.cafeadmin.identity.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "staff_users", schema = "identity")
public class StaffUser {

    @Id
    private UUID id;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    private String name;

    private String role;

    private String initials;

    @Column(name = "is_active")
    private boolean active;

    public UUID getId() {
        return id;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getInitials() {
        return initials;
    }

    public boolean isActive() {
        return active;
    }
}
