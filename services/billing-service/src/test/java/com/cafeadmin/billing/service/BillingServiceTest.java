package com.cafeadmin.billing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cafeadmin.billing.client.IdentityClient;
import com.cafeadmin.billing.client.OrderingClient;
import com.cafeadmin.billing.repo.InvoiceRepository;
import com.cafeadmin.common.web.ApiException;

class BillingServiceTest {

    private static final String RESTAURANT_ID = "11111111-1111-1111-1111-111111111111";

    private InvoiceRepository invoices;
    private OrderingClient ordering;
    private IdentityClient identity;
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        invoices = mock(InvoiceRepository.class);
        ordering = mock(OrderingClient.class);
        identity = mock(IdentityClient.class);
        when(identity.restaurant()).thenReturn(new IdentityClient.Restaurant("Café Admin", "Restaurant Management",
                "29ABCDE1234F1Z5", "12 Brew Street", "+91 80 4123 7788", new BigDecimal("0.0500"), "INR",
                "Asia/Kolkata"));
        when(invoices.findByRestaurantIdAndOrderNo(any(UUID.class), anyString())).thenReturn(Optional.empty());
        billingService = new BillingService(invoices, ordering, identity, RESTAURANT_ID, "Asia/Kolkata");
    }

    private void stubOrder() {
        when(ordering.findByOrderNo(anyString())).thenReturn(Optional.of(new OrderingClient.OrderDetail(
                UUID.randomUUID().toString(), "#041", "T-03", "06 Nov 2025", "10:15 AM", "completed", "staff",
                "no sugar", "Ada",
                List.of(new OrderingClient.OrderLine("Latte", 2, List.of("Oat Milk"), new BigDecimal("180.00"),
                        new BigDecimal("440.00"))),
                new BigDecimal("440.00"), 2)));
    }

    @Test
    void kotBillHidesPricing() {
        stubOrder();

        Map<String, Object> bill = billingService.bill("#041", "kot");

        assertThat(bill.get("title")).isEqualTo("KITCHEN ORDER TICKET");
        assertThat(bill.get("showPricing")).isEqualTo(false);
        assertThat(bill).doesNotContainKey("totals");
        assertThat(bill.get("orderNo")).isEqualTo("#041");
    }

    @SuppressWarnings("unchecked")
    @Test
    void taxInvoiceSplitsGstIntoCgstAndSgst() {
        stubOrder();

        Map<String, Object> totals = (Map<String, Object>) billingService.bill("#041", "ca").get("totals");

        assertThat((BigDecimal) totals.get("gst")).isEqualByComparingTo("22");
        assertThat((BigDecimal) totals.get("cgst")).isEqualByComparingTo("11");
        assertThat((BigDecimal) totals.get("sgst")).isEqualByComparingTo("11");
        assertThat((BigDecimal) totals.get("total")).isEqualByComparingTo("462.00");
    }

    @Test
    void rejectsUnknownFormat() {
        assertThatThrownBy(() -> billingService.bill("#041", "poster"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void missingOrderIsNotFound() {
        when(ordering.findByOrderNo(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.bill("#999", "kot"))
                .isInstanceOf(ApiException.class);
    }
}
