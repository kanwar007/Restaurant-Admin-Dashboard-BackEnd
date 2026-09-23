package com.cafeadmin.ordering.api.dto;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.cafeadmin.ordering.domain.OrderEntity;
import com.cafeadmin.ordering.domain.OrderItemAddonEntity;
import com.cafeadmin.ordering.domain.OrderItemEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

public final class OrderDtos {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private OrderDtos() {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderItemView(String name, int quantity, List<String> addons) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderView(String id, String orderNo, String table, String time, String date, String status,
            String source, List<OrderItemView> items, String notes, String customerName) {

        public static OrderView of(OrderEntity order, ZoneId zone) {
            List<OrderItemView> items = order.getItems().stream()
                    .map(item -> new OrderItemView(item.getItemName(), item.getQuantity(), addonNames(item)))
                    .toList();
            return new OrderView(order.getId().toString(), order.getOrderNo(), order.getTableNumber(),
                    TIME.format(order.getPlacedAt().atZone(zone)), DATE.format(order.getPlacedAt().atZone(zone)),
                    order.getStatus(), "guest".equals(order.getSource()) ? "guest" : null, items, order.getNotes(),
                    order.getCustomerName());
        }

        private static List<String> addonNames(OrderItemEntity item) {
            List<String> names = item.getAddons().stream().map(OrderItemAddonEntity::getAddonName).sorted().toList();
            return names.isEmpty() ? null : names;
        }
    }

    public record OrderLine(String name, int quantity, List<String> addons, BigDecimal unitPrice, BigDecimal amount) {
    }

    /** Snapshot shared with billing and reporting over the internal API. */
    public record OrderDetail(String id, String orderNo, String table, String date, String time, String status,
            String source, String notes, String customerName, List<OrderLine> lines, BigDecimal subtotal,
            int itemCount) {

        public static OrderDetail of(OrderEntity order, ZoneId zone) {
            List<OrderLine> lines = order.getItems().stream()
                    .map(item -> new OrderLine(item.getItemName(), item.getQuantity(),
                            item.getAddons().stream().map(OrderItemAddonEntity::getAddonName).sorted().toList(),
                            item.getUnitPrice(), item.getLineTotal()))
                    .toList();
            return new OrderDetail(order.getId().toString(), order.getOrderNo(), order.getTableNumber(),
                    DATE.format(order.getPlacedAt().atZone(zone)), TIME.format(order.getPlacedAt().atZone(zone)),
                    order.getStatus(), order.getSource(), order.getNotes(), order.getCustomerName(), lines,
                    order.subtotal(), order.getItems().stream().mapToInt(OrderItemEntity::getQuantity).sum());
        }
    }

    public record GuestOrderRequest(String table, List<GuestOrderItem> items, String notes, String customerName) {
    }

    public record GuestOrderItem(String name, Integer quantity, List<String> addons) {
    }

    public record StatusPatch(String status) {
    }

    public record LiveStats(int totalOrdersToday, int pendingKot, int activeOrders, List<LatestOrder> latestOrders) {
    }

    public record LatestOrder(String id, String orderNo, String table, int items, String placedAgo, String status) {
    }
}
