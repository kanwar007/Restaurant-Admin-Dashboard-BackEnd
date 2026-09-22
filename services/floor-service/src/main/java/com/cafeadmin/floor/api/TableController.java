package com.cafeadmin.floor.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cafeadmin.floor.api.dto.TableDtos.OccupancyRequest;
import com.cafeadmin.floor.api.dto.TableDtos.TableInput;
import com.cafeadmin.floor.api.dto.TableDtos.TablePatch;
import com.cafeadmin.floor.api.dto.TableDtos.TableResponse;
import com.cafeadmin.floor.service.FloorService;

@RestController
public class TableController {

    private final FloorService floorService;

    public TableController(FloorService floorService) {
        this.floorService = floorService;
    }

    @GetMapping("/api/tables")
    public List<TableResponse> list() {
        return floorService.list();
    }

    @PostMapping("/api/tables")
    @ResponseStatus(HttpStatus.CREATED)
    public TableResponse create(@RequestBody TableInput input) {
        return floorService.create(input);
    }

    @PatchMapping("/api/tables/{id}")
    public TableResponse update(@PathVariable UUID id, @RequestBody TablePatch patch) {
        return floorService.update(id, patch);
    }

    @PostMapping("/internal/tables/occupancy")
    public TableResponse occupy(@RequestBody OccupancyRequest request) {
        return floorService.occupy(request);
    }

    @PostMapping("/internal/tables/release")
    public Map<String, String> release(@RequestBody Map<String, String> request) {
        floorService.releaseForOrder(request.get("orderNo"));
        return Map.of("status", "released");
    }
}
