package com.cafeadmin.catalog.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_item_addons", schema = "catalog")
@IdClass(MenuItemAddon.Key.class)
public class MenuItemAddon {

    @Id
    @Column(name = "menu_item_id")
    private UUID menuItemId;

    @Id
    @Column(name = "addon_id")
    private UUID addonId;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    protected MenuItemAddon() {
    }

    public MenuItemAddon(UUID menuItemId, UUID addonId) {
        this.menuItemId = menuItemId;
        this.addonId = addonId;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public UUID getAddonId() {
        return addonId;
    }

    public static class Key implements Serializable {

        private UUID menuItemId;
        private UUID addonId;

        public Key() {
        }

        public Key(UUID menuItemId, UUID addonId) {
            this.menuItemId = menuItemId;
            this.addonId = addonId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return Objects.equals(menuItemId, key.menuItemId) && Objects.equals(addonId, key.addonId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(menuItemId, addonId);
        }
    }
}
