package com.cafeadmin.ordering.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OrderEntityTest {

    private static final UUID RESTAURANT = UUID.randomUUID();

    @Test
    void formatsOrderNumberForTheFrontend() {
        OrderEntity order = new OrderEntity(RESTAURANT, 7, "T-04", "guest", "Ada", null);

        assertThat(order.getOrderNo()).isEqualTo("#007");
        assertThat(order.getStatus()).isEqualTo("new");
    }

    @Test
    void subtotalIncludesAddonsPerUnit() {
        OrderEntity order = new OrderEntity(RESTAURANT, 1, "T-01", "staff", null, null);
        OrderItemEntity latte = new OrderItemEntity(UUID.randomUUID(), "Latte", new BigDecimal("180.00"), 2, null);
        latte.addAddon(new OrderItemAddonEntity(UUID.randomUUID(), "Oat Milk", new BigDecimal("40.00"), 2));
        order.addItem(latte);
        order.addItem(new OrderItemEntity(UUID.randomUUID(), "Croissant", new BigDecimal("120.00"), 1, null));

        assertThat(latte.getLineTotal()).isEqualByComparingTo("440.00");
        assertThat(order.subtotal()).isEqualByComparingTo("560.00");
        assertThat(order.getItems()).extracting(OrderItemEntity::getLineNo).containsExactly(1, 2);
    }

    @Test
    void cancellingKeepsStatusHistoryConsistent() {
        OrderEntity order = new OrderEntity(RESTAURANT, 2, "T-02", "staff", null, null);

        order.changeStatus("kot-printed");
        assertThat(order.getStatus()).isEqualTo("kot-printed");

        order.changeStatus("cancelled");
        assertThat(order.getStatus()).isEqualTo("cancelled");
    }
}
