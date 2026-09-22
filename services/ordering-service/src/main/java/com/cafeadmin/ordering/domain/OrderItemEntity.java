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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items", schema = "ordering")
public class OrderItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Column(name = "menu_item_id")
    private UUID menuItemId;

    @Column(name = "item_name_snapshot")
    private String itemName;

    @Column(name = "unit_price_snapshot")
    private BigDecimal unitPrice;

    private int quantity;

    @Column(name = "line_total")
    private BigDecimal lineTotal;

    private String notes;

    @Column(name = "line_no")
    private int lineNo;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItemAddonEntity> addons = new ArrayList<>();

    protected OrderItemEntity() {
    }

    public OrderItemEntity(UUID menuItemId, String itemName, BigDecimal unitPrice, int quantity, String notes) {
        this.id = UUID.randomUUID();
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.notes = notes;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public String getNotes() {
        return notes;
    }

    public int getLineNo() {
        return lineNo;
    }

    public List<OrderItemAddonEntity> getAddons() {
        return addons;
    }

    void attachTo(OrderEntity order) {
        this.order = order;
    }

    void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public void addAddon(OrderItemAddonEntity addon) {
        addon.attachTo(this);
        addons.add(addon);
        this.lineTotal = this.lineTotal.add(addon.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
    }
}
