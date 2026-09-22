package com.cafeadmin.ordering.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafeadmin.common.web.ApiException;
import com.cafeadmin.ordering.api.dto.OrderDtos.GuestOrderItem;
import com.cafeadmin.ordering.api.dto.OrderDtos.GuestOrderRequest;
import com.cafeadmin.ordering.api.dto.OrderDtos.LatestOrder;
import com.cafeadmin.ordering.api.dto.OrderDtos.LiveStats;
import com.cafeadmin.ordering.api.dto.OrderDtos.OrderDetail;
import com.cafeadmin.ordering.api.dto.OrderDtos.OrderView;
import com.cafeadmin.ordering.client.CatalogClient;
import com.cafeadmin.ordering.client.FloorClient;
import com.cafeadmin.ordering.domain.OrderEntity;
import com.cafeadmin.ordering.domain.OrderItemAddonEntity;
import com.cafeadmin.ordering.domain.OrderItemEntity;
import com.cafeadmin.ordering.domain.OrderStatusHistoryEntity;
import com.cafeadmin.ordering.repo.OrderRepository;
import com.cafeadmin.ordering.repo.OrderStatusHistoryRepository;

@Service
public class OrderService {

    private static final Set<String> ACTIVE_STATUSES = Set.of("new", "kot-printed", "served");
    private static final Set<String> ASSIGNABLE_STATUSES = Set.of("new", "kot-printed", "served");
    private static final Set<String> CLOSED_STATUSES = Set.of("completed", "cancelled");

    private final OrderRepository orders;
    private final OrderStatusHistoryRepository statusHistory;
    private final OrderNumberAllocator allocator;
    private final CatalogClient catalog;
    private final FloorClient floor;
    private final UUID restaurantId;
    private final ZoneId zone;

    public OrderService(OrderRepository orders, OrderStatusHistoryRepository statusHistory,
            OrderNumberAllocator allocator, CatalogClient catalog, FloorClient floor,
            @Value("${app.restaurant-id}") String restaurantId, @Value("${app.timezone}") String timezone) {
        this.orders = orders;
        this.statusHistory = statusHistory;
        this.allocator = allocator;
        this.catalog = catalog;
        this.floor = floor;
        this.restaurantId = UUID.fromString(restaurantId);
        this.zone = ZoneId.of(timezone);
    }

    @Transactional(readOnly = true)
    public List<OrderView> listActive() {
        return activeOrders().stream().map(order -> OrderView.of(order, zone)).toList();
    }

    @Transactional
    public OrderView updateStatus(UUID id, String status) {
        if (!ASSIGNABLE_STATUSES.contains(status)) {
            throw ApiException.badRequest("status must be one of new, kot-printed, served");
        }
        OrderEntity order = orders.findById(id)
                .filter(entry -> ACTIVE_STATUSES.contains(entry.getStatus()))
                .orElseThrow(() -> ApiException.notFound("Not found"));
        statusHistory.save(new OrderStatusHistoryEntity(order.getId(), order.getStatus(), status));
        order.changeStatus(status);
        return OrderView.of(order, zone);
    }

    @Transactional
    public void cancel(UUID id) {
        OrderEntity order = orders.findById(id)
                .filter(entry -> ACTIVE_STATUSES.contains(entry.getStatus()))
                .orElseThrow(() -> ApiException.notFound("Not found"));
        statusHistory.save(new OrderStatusHistoryEntity(order.getId(), order.getStatus(), "cancelled"));
        order.changeStatus("cancelled");
        floor.release(order.getOrderNo());
    }

    @Transactional
    public OrderView placeGuestOrder(GuestOrderRequest request) {
        if (request == null || request.table() == null || request.table().isBlank()) {
            throw ApiException.badRequest("table is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw ApiException.badRequest("items must be a non-empty array");
        }

        OrderEntity order = new OrderEntity(restaurantId, allocator.next(restaurantId), request.table().trim(),
                "guest", blankToNull(request.customerName()), blankToNull(request.notes()));

        for (GuestOrderItem item : request.items()) {
            int quantity = item.quantity() == null ? 1 : item.quantity();
            if (quantity < 1) {
                throw ApiException.badRequest("quantity must be a positive whole number");
            }
            CatalogClient.MenuItemView menuItem = catalog.findMenuItem(item.name())
                    .filter(CatalogClient.MenuItemView::available)
                    .orElseThrow(() -> ApiException.badRequest(item.name() + " is not on the menu right now"));

            OrderItemEntity orderItem = new OrderItemEntity(UUID.fromString(menuItem.id()), menuItem.name(),
                    menuItem.price(), quantity, null);
            if (item.addons() != null) {
                for (String addonName : item.addons()) {
                    CatalogClient.AddonView addon = catalog.findAddon(addonName)
                            .orElseThrow(() -> ApiException.badRequest(addonName + " is not an available add-on"));
                    orderItem.addAddon(new OrderItemAddonEntity(UUID.fromString(addon.id()), addon.name(),
                            addon.price(), quantity));
                }
            }
            order.addItem(orderItem);
        }

        OrderEntity saved = orders.save(order);
        statusHistory.save(new OrderStatusHistoryEntity(saved.getId(), null, saved.getStatus()));
        floor.occupy(saved.getTableNumber(), saved.getId(), saved.getOrderNo());
        return OrderView.of(saved, zone);
    }

    @Transactional(readOnly = true)
    public OrderDetail detailByOrderNo(String orderNo) {
        long number = parseOrderNumber(orderNo);
        return orders.findByRestaurantIdAndOrderNumber(restaurantId, number)
                .map(order -> OrderDetail.of(order, zone))
                .orElseThrow(() -> ApiException.notFound("Not found"));
    }

    @Transactional(readOnly = true)
    public List<OrderDetail> closedOrders() {
        return orders.findByStatuses(restaurantId, CLOSED_STATUSES).stream()
                .map(order -> OrderDetail.of(order, zone))
                .toList();
    }

    @Transactional(readOnly = true)
    public LiveStats stats() {
        List<OrderEntity> active = activeOrders();
        LocalDate today = LocalDate.now(zone);
        int todaysOrders = (int) orders.findByStatuses(restaurantId,
                        List.of("new", "kot-printed", "served", "completed", "cancelled")).stream()
                .filter(order -> order.getPlacedAt().atZone(zone).toLocalDate().equals(today))
                .count();
        int pendingKot = (int) active.stream().filter(order -> "new".equals(order.getStatus())).count();
        List<LatestOrder> latest = active.stream()
                .limit(5)
                .map(order -> new LatestOrder(order.getId().toString(), order.getOrderNo(), order.getTableNumber(),
                        order.getItems().stream().mapToInt(OrderItemEntity::getQuantity).sum(),
                        placedAgo(order.getPlacedAt()), order.getStatus()))
                .toList();
        return new LiveStats(todaysOrders, pendingKot, active.size(), latest);
    }

    private List<OrderEntity> activeOrders() {
        return orders.findByStatuses(restaurantId, ACTIVE_STATUSES);
    }

    private static String placedAgo(Instant placedAt) {
        long minutes = Math.max(0, Duration.between(placedAt, Instant.now()).toMinutes());
        if (minutes < 60) {
            return minutes + " min ago";
        }
        long hours = minutes / 60;
        return hours + (hours == 1 ? " hour ago" : " hours ago");
    }

    private static long parseOrderNumber(String orderNo) {
        String digits = orderNo == null ? "" : orderNo.replace("#", "").trim();
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException exception) {
            throw ApiException.notFound("Not found");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
