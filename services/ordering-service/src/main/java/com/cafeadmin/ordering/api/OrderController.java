package com.cafeadmin.ordering.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cafeadmin.ordering.api.dto.OrderDtos.GuestOrderRequest;
import com.cafeadmin.ordering.api.dto.OrderDtos.LiveStats;
import com.cafeadmin.ordering.api.dto.OrderDtos.OrderDetail;
import com.cafeadmin.ordering.api.dto.OrderDtos.OrderView;
import com.cafeadmin.ordering.api.dto.OrderDtos.StatusPatch;
import com.cafeadmin.ordering.service.OrderService;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/api/orders")
    public List<OrderView> list() {
        return orderService.listActive();
    }

    @PatchMapping("/api/orders/{id}/status")
    public OrderView updateStatus(@PathVariable UUID id, @RequestBody StatusPatch patch) {
        return orderService.updateStatus(id, patch == null ? null : patch.status());
    }

    @DeleteMapping("/api/orders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id) {
        orderService.cancel(id);
    }

    @PostMapping("/api/guest/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderView guestOrder(@RequestBody GuestOrderRequest request) {
        return orderService.placeGuestOrder(request);
    }

    @GetMapping("/internal/orders/by-number/{orderNo}")
    public OrderDetail detail(@PathVariable String orderNo) {
        return orderService.detailByOrderNo(orderNo);
    }

    @GetMapping("/internal/orders/closed")
    public List<OrderDetail> closed() {
        return orderService.closedOrders();
    }

    @GetMapping("/internal/orders/stats")
    public LiveStats stats() {
        return orderService.stats();
    }
}
