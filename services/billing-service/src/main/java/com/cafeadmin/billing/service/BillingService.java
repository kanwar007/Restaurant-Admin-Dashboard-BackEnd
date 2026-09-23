package com.cafeadmin.billing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafeadmin.billing.client.IdentityClient;
import com.cafeadmin.billing.client.OrderingClient;
import com.cafeadmin.billing.domain.Invoice;
import com.cafeadmin.billing.domain.InvoiceLine;
import com.cafeadmin.billing.repo.InvoiceRepository;
import com.cafeadmin.common.web.ApiException;

@Service
public class BillingService {

    private static final Set<String> FORMATS = Set.of("kot", "customer", "ca", "restaurant");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final InvoiceRepository invoices;
    private final OrderingClient ordering;
    private final IdentityClient identity;
    private final UUID restaurantId;
    private final ZoneId zone;

    public BillingService(InvoiceRepository invoices, OrderingClient ordering, IdentityClient identity,
            @Value("${app.restaurant-id}") String restaurantId, @Value("${app.timezone}") String timezone) {
        this.invoices = invoices;
        this.ordering = ordering;
        this.identity = identity;
        this.restaurantId = UUID.fromString(restaurantId);
        this.zone = ZoneId.of(timezone);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> bill(String orderNo, String format) {
        String requested = format == null || format.isBlank() ? "kot" : format;
        if (!FORMATS.contains(requested)) {
            throw ApiException.badRequest("format must be one of kot, customer, ca, restaurant");
        }
        OrderingClient.OrderDetail order = ordering.findByOrderNo(orderNo)
                .orElseThrow(() -> ApiException.notFound("Not found"));
        IdentityClient.Restaurant restaurant = identity.restaurant();

        List<Map<String, Object>> lines = order.lines().stream()
                .map(line -> {
                    Map<String, Object> rendered = new LinkedHashMap<>();
                    rendered.put("name", line.name());
                    rendered.put("quantity", line.quantity());
                    rendered.put("addons", line.addons() == null ? List.of() : line.addons());
                    rendered.put("unitPrice", line.unitPrice());
                    rendered.put("amount", line.amount());
                    return rendered;
                })
                .toList();

        BigDecimal subtotal = order.subtotal();
        BigDecimal gstRate = restaurant.gstRate() == null ? new BigDecimal("0.05") : restaurant.gstRate();
        BigDecimal gst = subtotal.multiply(gstRate).setScale(0, RoundingMode.HALF_UP);

        Map<String, Object> bill = new LinkedHashMap<>();
        bill.put("format", requested);
        bill.put("orderNo", order.orderNo());
        bill.put("table", order.table());
        bill.put("date", order.date());
        bill.put("time", order.time());
        bill.put("notes", order.notes());
        bill.put("lines", lines);
        bill.put("restaurant", restaurant.asBillHeader());

        switch (requested) {
            case "kot" -> {
                bill.put("title", "KITCHEN ORDER TICKET");
                bill.put("showPricing", false);
            }
            case "ca" -> {
                BigDecimal halfGst = gst.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
                bill.put("title", "TAX INVOICE");
                bill.put("showPricing", true);
                bill.put("totals", Map.of("subtotal", subtotal, "cgst", halfGst, "sgst", gst.subtract(halfGst),
                        "gst", gst, "total", subtotal.add(gst)));
            }
            case "restaurant" -> {
                bill.put("title", "RESTAURANT COPY");
                bill.put("showPricing", true);
                bill.put("totals", totals(subtotal, gst));
                bill.put("settlement", Map.of("paymentMode",
                        invoices.findByRestaurantIdAndOrderNo(restaurantId, order.orderNo())
                                .map(Invoice::getPaymentMode).orElse("upi"), "server", "Admin User"));
            }
            default -> {
                bill.put("title", restaurant.name());
                bill.put("showPricing", true);
                bill.put("totals", totals(subtotal, gst));
                bill.put("footer", "Thank you for dining with us!");
            }
        }
        return bill;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> history(String search, String status) {
        List<Map<String, Object>> rows = new ArrayList<>();
        invoices.findByRestaurantIdOrderByIssuedAtDesc(restaurantId).forEach(invoice -> rows.add(historyRow(invoice)));
        ordering.closedOrders().stream()
                .filter(order -> "cancelled".equals(order.status()))
                .forEach(order -> rows.add(cancelledRow(order)));

        List<Map<String, Object>> filtered = rows.stream()
                .filter(row -> status == null || status.isBlank() || "all".equals(status)
                        || status.equals(row.get("status")))
                .filter(row -> search == null || search.isBlank()
                        || contains((String) row.get("orderNo"), search) || contains((String) row.get("table"), search))
                .sorted(Comparator.comparing((Map<String, Object> row) -> (String) row.get("orderNo")).reversed())
                .toList();

        BigDecimal revenue = filtered.stream()
                .map(row -> (BigDecimal) row.get("totalAmount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of("rows", filtered, "summary", Map.of(
                "totalOrders", filtered.size(),
                "completed", filtered.stream().filter(row -> "completed".equals(row.get("status"))).count(),
                "cancelled", filtered.stream().filter(row -> "cancelled".equals(row.get("status"))).count(),
                "totalRevenue", revenue));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> todaysRevenue() {
        LocalDate today = LocalDate.now(zone);
        List<Invoice> issuedToday = invoices.findByRestaurantIdOrderByIssuedAtDesc(restaurantId).stream()
                .filter(invoice -> invoice.getIssuedAt().atZone(zone).toLocalDate().equals(today))
                .toList();
        BigDecimal revenue = issuedToday.stream().map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("revenue", revenue, "bills", issuedToday.size());
    }

    private Map<String, Object> historyRow(Invoice invoice) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", invoice.getId().toString());
        row.put("orderNo", invoice.getOrderNo());
        row.put("table", invoice.getTableNumber());
        row.put("date", DATE.format(invoice.getIssuedAt().atZone(zone)));
        row.put("time", TIME.format(invoice.getIssuedAt().atZone(zone)));
        row.put("status", "completed");
        row.put("totalAmount", invoice.getTotal());
        row.put("paymentMode", invoice.getPaymentMode());
        row.put("items", invoice.getLines().stream().mapToInt(InvoiceLine::getQuantity).sum());
        return row;
    }

    private Map<String, Object> cancelledRow(OrderingClient.OrderDetail order) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", order.id());
        row.put("orderNo", order.orderNo());
        row.put("table", order.table());
        row.put("date", order.date());
        row.put("time", order.time());
        row.put("status", "cancelled");
        row.put("totalAmount", BigDecimal.ZERO);
        row.put("paymentMode", "cash");
        row.put("items", order.itemCount());
        return row;
    }

    private static Map<String, Object> totals(BigDecimal subtotal, BigDecimal gst) {
        return Map.of("subtotal", subtotal, "gst", gst, "total", subtotal.add(gst));
    }

    private static boolean contains(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }
}
