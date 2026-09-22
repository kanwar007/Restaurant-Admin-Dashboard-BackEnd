package com.cafeadmin.identity.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "restaurants", schema = "identity")
public class Restaurant {

    @Id
    private UUID id;

    private String code;

    private String name;

    private String tagline;

    private String gstin;

    private String address;

    private String phone;

    private String timezone;

    private String currency;

    @Column(name = "tax_rate")
    private BigDecimal taxRate;

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getTagline() {
        return tagline;
    }

    public String getGstin() {
        return gstin;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }
}
