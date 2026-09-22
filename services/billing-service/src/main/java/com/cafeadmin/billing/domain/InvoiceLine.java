package com.cafeadmin.billing.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoice_lines", schema = "billing")
public class InvoiceLine {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    private String description;

    private int quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    private BigDecimal amount;

    @Column(name = "line_no")
    private int lineNo;

    protected InvoiceLine() {
    }

    public InvoiceLine(String description, int quantity, BigDecimal unitPrice, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    void attachTo(Invoice invoice, int lineNo) {
        this.invoice = invoice;
        this.lineNo = lineNo;
    }
}
