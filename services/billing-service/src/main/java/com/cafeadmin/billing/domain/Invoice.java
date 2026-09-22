package com.cafeadmin.billing.domain;

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
@Table(name = "invoices", schema = "billing")
public class Invoice {

    @Id
    private UUID id;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "table_number")
    private String tableNumber;

    private BigDecimal subtotal;

    @Column(name = "tax_rate")
    private BigDecimal taxRate;

    @Column(name = "tax_total")
    private BigDecimal taxTotal;

    private BigDecimal total;

    @Column(name = "payment_mode")
    private String paymentMode;

    private String status;

    @Column(name = "issued_at")
    private Instant issuedAt = Instant.now();

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Version
    private int version;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("lineNo ASC")
    private List<InvoiceLine> lines = new ArrayList<>();

    protected Invoice() {
    }

    public Invoice(UUID restaurantId, UUID orderId, String orderNo, String tableNumber, BigDecimal subtotal,
            BigDecimal taxRate, BigDecimal taxTotal, String paymentMode) {
        this.id = UUID.randomUUID();
        this.restaurantId = restaurantId;
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.tableNumber = tableNumber;
        this.subtotal = subtotal;
        this.taxRate = taxRate;
        this.taxTotal = taxTotal;
        this.total = subtotal.add(taxTotal);
        this.paymentMode = paymentMode;
        this.status = "paid";
    }

    public UUID getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxTotal() {
        return taxTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getStatus() {
        return status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }

    public void addLine(InvoiceLine line) {
        line.attachTo(this, lines.size() + 1);
        lines.add(line);
    }
}
