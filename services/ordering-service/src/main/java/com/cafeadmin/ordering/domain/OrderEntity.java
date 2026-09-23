package com.cafeadmin.ordering.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders", schema = "ordering")
public class OrderEntity {

    @Id
    private UUID id;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "order_number")
    private long orderNumber;

    @Column(name = "table_id")
    private UUID tableId;

    @Column(name = "table_number_snapshot")
    private String tableNumber;

    private String status;

    private String source;

    @Column(name = "customer_name")
    private String customerName;

    private String notes;

    @Column(name = "placed_at")
    private Instant placedAt = Instant.now();

    @Column(name = "served_at")
    private Instant servedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Version
    private int version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("lineNo ASC")
    private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {
    }

    public OrderEntity(UUID restaurantId, long orderNumber, String tableNumber, String source, String customerName,
            String notes) {
        this.id = UUID.randomUUID();
        this.restaurantId = restaurantId;
        this.orderNumber = orderNumber;
        this.tableNumber = tableNumber;
        this.source = source;
        this.customerName = customerName;
        this.notes = notes;
        this.status = "new";
    }

    public UUID getId() {
        return id;
    }

    public long getOrderNumber() {
        return orderNumber;
    }

    public String getOrderNo() {
        return String.format("#%03d", orderNumber);
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getSource() {
        return source;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public void addItem(OrderItemEntity item) {
        item.attachTo(this);
        item.setLineNo(items.size() + 1);
        items.add(item);
    }

    public void changeStatus(String next) {
        this.status = next;
        this.updatedAt = Instant.now();
        if ("served".equals(next) || "completed".equals(next)) {
            this.servedAt = Instant.now();
        }
        if ("cancelled".equals(next)) {
            this.cancelledAt = Instant.now();
        }
    }

    public BigDecimal subtotal() {
        return items.stream().map(OrderItemEntity::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
