package com.cafeadmin.catalog.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cafeadmin.catalog.api.dto.MenuItemInput;
import com.cafeadmin.catalog.api.dto.MenuItemResponse;
import com.cafeadmin.catalog.service.CatalogService;

@RestController
public class MenuController {

    private final CatalogService catalogService;

    public MenuController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/api/menu/categories")
    public List<String> categories() {
        return catalogService.categoryNames();
    }

    @GetMapping("/api/menu")
    public List<MenuItemResponse> list(@RequestParam(required = false) String category,
                                       @RequestParam(required = false) String search) {
        return catalogService.listMenu(category, search);
    }

    @PostMapping("/api/menu")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse create(@RequestBody MenuItemInput input) {
        return catalogService.createMenuItem(input);
    }

    @PatchMapping("/api/menu/{id}")
    public MenuItemResponse update(@PathVariable UUID id, @RequestBody MenuItemInput input) {
        return catalogService.updateMenuItem(id, input);
    }

    @DeleteMapping("/api/menu/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        catalogService.deleteMenuItem(id);
    }

    @GetMapping("/internal/menu-items/by-name/{name}")
    public MenuItemResponse lookup(@PathVariable String name) {
        return catalogService.lookupByName(name);
    }
}
