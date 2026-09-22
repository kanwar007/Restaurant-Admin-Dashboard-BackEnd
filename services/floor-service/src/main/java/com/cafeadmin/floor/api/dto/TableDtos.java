package com.cafeadmin.floor.api.dto;

import java.util.UUID;

import com.cafeadmin.floor.domain.DiningTable;
import com.fasterxml.jackson.annotation.JsonInclude;

public final class TableDtos {

    private TableDtos() {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TableResponse(String id, String number, int capacity, String status, String currentOrder) {

        public static TableResponse of(DiningTable table) {
            return new TableResponse(table.getId().toString(), table.getNumber(), table.getCapacity(),
                    table.getStatus(), table.getCurrentOrderDisplay());
        }
    }

    public record TableInput(String number, Integer capacity) {
    }

    public record TablePatch(String number, Integer capacity, String status) {
    }

    public record OccupancyRequest(String table, UUID orderId, String orderNo) {
    }
}
