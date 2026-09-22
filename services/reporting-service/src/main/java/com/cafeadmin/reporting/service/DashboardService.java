package com.cafeadmin.reporting.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cafeadmin.reporting.client.UpstreamClients;
import com.cafeadmin.reporting.client.UpstreamClients.LatestOrder;
import com.cafeadmin.reporting.client.UpstreamClients.LiveStats;
import com.cafeadmin.reporting.client.UpstreamClients.TableView;

@Service
public class DashboardService {

    private static final DateTimeFormatter BUSINESS_DATE =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter BUSINESS_TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DecimalFormat RUPEES = new DecimalFormat("#,##0");

    private final UpstreamClients upstream;
    private final ZoneId zone;

    public DashboardService(UpstreamClients upstream, @Value("${app.timezone}") String timezone) {
        this.upstream = upstream;
        this.zone = ZoneId.of(timezone);
    }

    public Map<String, Object> dashboard() {
        LiveStats stats = upstream.orderStats();
        List<TableView> tables = upstream.tables();
        BigDecimal revenue = upstream.revenueToday();

        long occupied = tables.stream().filter(table -> !"vacant".equals(table.status())).count();
        int occupancyPercent = tables.isEmpty() ? 0 : (int) Math.round(occupied * 100.0 / tables.size());

        ZonedDateTime now = ZonedDateTime.now(zone);
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("businessDate", BUSINESS_DATE.format(now));
        dashboard.put("businessTime", BUSINESS_TIME.format(now));
        dashboard.put("stats", List.of(
                stat("orders", "Total Orders Today", String.valueOf(stats.totalOrdersToday()),
                        stats.activeOrders() + " active", "clipboard"),
                stat("tables", "Active Tables", occupied + "/" + tables.size(),
                        occupancyPercent + "% occupied", "users"),
                stat("kot", "Pending KOT", String.valueOf(stats.pendingKot()),
                        stats.pendingKot() == 0 ? "all clear" : "awaiting print", "clock"),
                stat("revenue", "Revenue Today", "₹" + RUPEES.format(revenue.setScale(0, RoundingMode.HALF_UP)),
                        "billed today", "rupee")));
        dashboard.put("latestOrders", stats.latestOrders().stream().map(DashboardService::latestOrder).toList());
        return dashboard;
    }

    private static Map<String, Object> stat(String id, String label, String value, String delta, String icon) {
        Map<String, Object> stat = new LinkedHashMap<>();
        stat.put("id", id);
        stat.put("label", label);
        stat.put("value", value);
        stat.put("delta", delta);
        stat.put("tone", "positive");
        stat.put("icon", icon);
        return stat;
    }

    private static Map<String, Object> latestOrder(LatestOrder order) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", order.id());
        row.put("orderNo", order.orderNo());
        row.put("table", order.table());
        row.put("items", order.items());
        row.put("placedAgo", order.placedAgo());
        row.put("status", order.status());
        return row;
    }
}
