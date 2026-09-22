package com.cafeadmin.billing.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cafeadmin.billing.service.BillingService;

@RestController
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/api/bills/{orderNo}")
    public Map<String, Object> bill(@PathVariable String orderNo,
                                    @RequestParam(required = false, defaultValue = "kot") String format) {
        return billingService.bill(orderNo, format);
    }

    @GetMapping("/api/order-history")
    public Map<String, Object> history(@RequestParam(required = false) String search,
                                       @RequestParam(required = false) String status) {
        return billingService.history(search, status);
    }

    @GetMapping("/internal/revenue/today")
    public Map<String, Object> todaysRevenue() {
        return billingService.todaysRevenue();
    }
}
