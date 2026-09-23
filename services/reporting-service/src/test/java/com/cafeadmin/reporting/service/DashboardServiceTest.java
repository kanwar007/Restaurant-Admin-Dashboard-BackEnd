package com.cafeadmin.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.cafeadmin.reporting.client.UpstreamClients;
import com.cafeadmin.reporting.client.UpstreamClients.LatestOrder;
import com.cafeadmin.reporting.client.UpstreamClients.LiveStats;
import com.cafeadmin.reporting.client.UpstreamClients.TableView;

class DashboardServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void aggregatesUpstreamDataIntoDashboardStats() {
        UpstreamClients upstream = mock(UpstreamClients.class);
        when(upstream.orderStats()).thenReturn(new LiveStats(12, 3, 5,
                List.of(new LatestOrder("id-1", "#009", "T-09", 2, "5 min ago", "new"))));
        when(upstream.tables()).thenReturn(List.of(
                new TableView("1", "T-01", 4, "occupied", "#001"),
                new TableView("2", "T-02", 2, "vacant", null),
                new TableView("3", "T-03", 4, "bill-pending", "#003"),
                new TableView("4", "T-04", 6, "vacant", null)));
        when(upstream.revenueToday()).thenReturn(new BigDecimal("4520.50"));

        Map<String, Object> dashboard = new DashboardService(upstream, "Asia/Kolkata").dashboard();

        List<Map<String, Object>> stats = (List<Map<String, Object>>) dashboard.get("stats");
        assertThat(stats).extracting(stat -> stat.get("id"))
                .containsExactly("orders", "tables", "kot", "revenue");
        assertThat(stats.get(0).get("value")).isEqualTo("12");
        assertThat(stats.get(1).get("value")).isEqualTo("2/4");
        assertThat(stats.get(1).get("delta")).isEqualTo("50% occupied");
        assertThat(stats.get(2).get("value")).isEqualTo("3");
        assertThat(stats.get(3).get("value")).isEqualTo("₹4,521");
        assertThat((List<Map<String, Object>>) dashboard.get("latestOrders")).hasSize(1);
        assertThat(dashboard.get("businessDate")).isNotNull();
    }

    @Test
    void survivesUpstreamOutages() {
        UpstreamClients upstream = mock(UpstreamClients.class);
        when(upstream.orderStats()).thenReturn(new LiveStats(0, 0, 0, List.of()));
        when(upstream.tables()).thenReturn(List.of());
        when(upstream.revenueToday()).thenReturn(BigDecimal.ZERO);

        Map<String, Object> dashboard = new DashboardService(upstream, "Asia/Kolkata").dashboard();

        assertThat(dashboard.get("latestOrders")).isEqualTo(List.of());
    }
}
