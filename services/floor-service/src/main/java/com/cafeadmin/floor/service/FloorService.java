package com.cafeadmin.floor.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafeadmin.common.web.ApiException;
import com.cafeadmin.floor.api.dto.TableDtos.OccupancyRequest;
import com.cafeadmin.floor.api.dto.TableDtos.TableInput;
import com.cafeadmin.floor.api.dto.TableDtos.TablePatch;
import com.cafeadmin.floor.api.dto.TableDtos.TableResponse;
import com.cafeadmin.floor.domain.DiningTable;
import com.cafeadmin.floor.repo.DiningTableRepository;

@Service
public class FloorService {

    private static final Set<String> ALLOWED_STATUS = Set.of("vacant", "occupied", "reserved", "bill-pending");

    private final DiningTableRepository tables;
    private final UUID restaurantId;

    public FloorService(DiningTableRepository tables, @Value("${app.restaurant-id}") String restaurantId) {
        this.tables = tables;
        this.restaurantId = UUID.fromString(restaurantId);
    }

    @Transactional(readOnly = true)
    public List<TableResponse> list() {
        return tables.findByRestaurantIdOrderByNumberAsc(restaurantId).stream()
                .map(TableResponse::of)
                .toList();
    }

    @Transactional
    public TableResponse create(TableInput input) {
        if (input.capacity() == null || input.capacity() < 1) {
            throw ApiException.badRequest("capacity is required");
        }
        String number = (input.number() == null || input.number().isBlank())
                ? nextTableNumber() : input.number().trim();
        tables.findByRestaurantIdAndNumberIgnoreCase(restaurantId, number).ifPresent(existing -> {
            throw ApiException.badRequest("Table " + existing.getNumber() + " already exists");
        });
        return TableResponse.of(tables.save(new DiningTable(restaurantId, number, input.capacity())));
    }

    @Transactional
    public TableResponse update(UUID id, TablePatch patch) {
        DiningTable table = tables.findById(id).orElseThrow(() -> ApiException.notFound("Not found"));
        if (patch.status() != null && !ALLOWED_STATUS.contains(patch.status())) {
            throw ApiException.badRequest("status must be one of " + String.join(", ", ALLOWED_STATUS));
        }
        if (patch.capacity() != null && patch.capacity() < 1) {
            throw ApiException.badRequest("capacity must be a positive whole number");
        }
        table.update(patch.number(), patch.capacity(), patch.status());
        return TableResponse.of(table);
    }

    @Transactional
    public TableResponse occupy(OccupancyRequest request) {
        DiningTable table = tables.findByRestaurantIdAndNumberIgnoreCase(restaurantId, request.table())
                .orElseThrow(() -> ApiException.notFound("Table " + request.table() + " does not exist"));
        table.occupy(request.orderId(), request.orderNo());
        return TableResponse.of(table);
    }

    @Transactional
    public void releaseForOrder(String orderNo) {
        tables.findByRestaurantIdAndCurrentOrderDisplay(restaurantId, orderNo).ifPresent(DiningTable::release);
    }

    private String nextTableNumber() {
        int next = tables.findByRestaurantIdOrderByNumberAsc(restaurantId).size() + 1;
        return String.format(Locale.ROOT, "T-%02d", next);
    }
}
