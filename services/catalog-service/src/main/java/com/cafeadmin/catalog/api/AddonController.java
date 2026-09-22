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

import com.cafeadmin.catalog.api.dto.AddonInput;
import com.cafeadmin.catalog.api.dto.AddonResponse;
import com.cafeadmin.catalog.service.CatalogService;

@RestController
public class AddonController {

    private final CatalogService catalogService;

    public AddonController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/api/addons")
    public List<AddonResponse> list(@RequestParam(required = false) String search) {
        return catalogService.listAddons(search);
    }

    @PostMapping("/api/addons")
    @ResponseStatus(HttpStatus.CREATED)
    public AddonResponse create(@RequestBody AddonInput input) {
        return catalogService.createAddon(input);
    }

    @PatchMapping("/api/addons/{id}")
    public AddonResponse update(@PathVariable UUID id, @RequestBody AddonInput input) {
        return catalogService.updateAddon(id, input);
    }

    @DeleteMapping("/api/addons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        catalogService.deleteAddon(id);
    }

    @GetMapping("/internal/addons/by-name/{name}")
    public AddonResponse lookup(@PathVariable String name) {
        return catalogService.lookupAddonByName(name);
    }
}
