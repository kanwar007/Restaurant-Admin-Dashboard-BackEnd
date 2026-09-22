package com.cafeadmin.catalog.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafeadmin.catalog.api.dto.AddonInput;
import com.cafeadmin.catalog.api.dto.AddonResponse;
import com.cafeadmin.catalog.api.dto.MenuItemInput;
import com.cafeadmin.catalog.api.dto.MenuItemResponse;
import com.cafeadmin.catalog.domain.Addon;
import com.cafeadmin.catalog.domain.Category;
import com.cafeadmin.catalog.domain.MenuItem;
import com.cafeadmin.catalog.domain.MenuItemAddon;
import com.cafeadmin.catalog.repo.AddonRepository;
import com.cafeadmin.catalog.repo.CategoryRepository;
import com.cafeadmin.catalog.repo.MenuItemAddonRepository;
import com.cafeadmin.catalog.repo.MenuItemRepository;
import com.cafeadmin.common.web.ApiException;

@Service
public class CatalogService {

    private final CategoryRepository categories;
    private final MenuItemRepository menuItems;
    private final AddonRepository addons;
    private final MenuItemAddonRepository links;
    private final UUID restaurantId;

    public CatalogService(CategoryRepository categories, MenuItemRepository menuItems, AddonRepository addons,
                          MenuItemAddonRepository links, @Value("${app.restaurant-id}") String restaurantId) {
        this.categories = categories;
        this.menuItems = menuItems;
        this.addons = addons;
        this.links = links;
        this.restaurantId = UUID.fromString(restaurantId);
    }

    @Transactional(readOnly = true)
    public List<String> categoryNames() {
        return categories.findByRestaurantIdAndActiveTrueOrderBySortOrderAscNameAsc(restaurantId).stream()
                .map(Category::getName)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> listMenu(String category, String search) {
        String categoryFilter = (category == null || category.isBlank() || "All".equalsIgnoreCase(category))
                ? null : category;
        String searchFilter = (search == null || search.isBlank()) ? null : search;
        Map<UUID, Long> counts = addonCounts();
        return menuItems.search(restaurantId, categoryFilter, searchFilter).stream()
                .map(item -> toResponse(item, counts))
                .toList();
    }

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemInput input) {
        if (input.name() == null || input.name().isBlank() || input.category() == null || input.category().isBlank()
                || input.price() == null) {
            throw ApiException.badRequest("name, category and price are required");
        }
        if (input.price().signum() < 0) {
            throw ApiException.badRequest("price must not be negative");
        }
        Category category = resolveCategory(input.category());
        MenuItem item = menuItems.save(new MenuItem(restaurantId, category, input.name().trim(), input.price(),
                input.available() == null || input.available()));
        return toResponse(item, addonCounts());
    }

    @Transactional
    public MenuItemResponse updateMenuItem(UUID id, MenuItemInput input) {
        MenuItem item = menuItems.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ApiException.notFound("Not found"));
        if (input.price() != null && input.price().signum() < 0) {
            throw ApiException.badRequest("price must not be negative");
        }
        Category category = (input.category() == null || input.category().isBlank())
                ? null : resolveCategory(input.category());
        item.update(input.name(), category, input.price(), input.available());
        return toResponse(item, addonCounts());
    }

    @Transactional
    public void deleteMenuItem(UUID id) {
        MenuItem item = menuItems.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ApiException.notFound("Not found"));
        links.deleteByMenuItemId(item.getId());
        item.softDelete();
    }

    @Transactional(readOnly = true)
    public List<AddonResponse> listAddons(String search) {
        Map<UUID, List<String>> dishes = linkedDishes();
        return addons.search(restaurantId, (search == null || search.isBlank()) ? null : search).stream()
                .map(addon -> toResponse(addon, dishes))
                .toList();
    }

    @Transactional
    public AddonResponse createAddon(AddonInput input) {
        if (input.name() == null || input.name().isBlank() || input.price() == null) {
            throw ApiException.badRequest("name and price are required");
        }
        if (input.price().signum() < 0) {
            throw ApiException.badRequest("price must not be negative");
        }
        Addon addon = addons.save(new Addon(restaurantId, input.name().trim(), input.price()));
        replaceLinks(addon, input.linkedDishes());
        return toResponse(addon, linkedDishes());
    }

    @Transactional
    public AddonResponse updateAddon(UUID id, AddonInput input) {
        Addon addon = addons.findById(id).orElseThrow(() -> ApiException.notFound("Not found"));
        if (input.price() != null && input.price().signum() < 0) {
            throw ApiException.badRequest("price must not be negative");
        }
        addon.update(input.name(), input.price());
        if (input.linkedDishes() != null) {
            replaceLinks(addon, input.linkedDishes());
        }
        return toResponse(addon, linkedDishes());
    }

    @Transactional
    public void deleteAddon(UUID id) {
        Addon addon = addons.findById(id).orElseThrow(() -> ApiException.notFound("Not found"));
        links.deleteByAddonId(addon.getId());
        addons.delete(addon);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse lookupByName(String name) {
        return menuItems.findByRestaurantIdAndNameIgnoreCaseAndDeletedAtIsNull(restaurantId, name)
                .map(item -> toResponse(item, addonCounts()))
                .orElseThrow(() -> ApiException.notFound(name + " is not on the menu"));
    }

    @Transactional(readOnly = true)
    public AddonResponse lookupAddonByName(String name) {
        return addons.findByRestaurantIdAndNameIgnoreCase(restaurantId, name)
                .map(addon -> toResponse(addon, linkedDishes()))
                .orElseThrow(() -> ApiException.notFound(name + " is not an add-on"));
    }

    private Category resolveCategory(String name) {
        return categories.findByRestaurantIdAndNameIgnoreCase(restaurantId, name.trim())
                .orElseGet(() -> categories.save(new Category(restaurantId, name.trim(),
                        categories.findByRestaurantIdAndActiveTrueOrderBySortOrderAscNameAsc(restaurantId).size() + 1)));
    }

    private void replaceLinks(Addon addon, List<String> dishNames) {
        links.deleteByAddonId(addon.getId());
        if (dishNames == null) {
            return;
        }
        List<MenuItemAddon> created = new ArrayList<>();
        for (String dishName : dishNames) {
            MenuItem item = menuItems
                    .findByRestaurantIdAndNameIgnoreCaseAndDeletedAtIsNull(restaurantId, dishName.trim())
                    .orElseThrow(() -> ApiException.badRequest(dishName + " is not on the menu"));
            created.add(new MenuItemAddon(item.getId(), addon.getId()));
        }
        links.saveAll(created);
    }

    private Map<UUID, Long> addonCounts() {
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : links.countByMenuItem()) {
            counts.put((UUID) row[0], (Long) row[1]);
        }
        return counts;
    }

    private Map<UUID, List<String>> linkedDishes() {
        Map<UUID, List<String>> dishes = new HashMap<>();
        for (Object[] row : links.linkedDishNames()) {
            dishes.computeIfAbsent((UUID) row[0], key -> new ArrayList<>()).add((String) row[1]);
        }
        return dishes;
    }

    private static MenuItemResponse toResponse(MenuItem item, Map<UUID, Long> counts) {
        return new MenuItemResponse(item.getId().toString(), item.getName(), item.getCategory().getName(),
                item.getPrice(), item.isAvailable(), counts.getOrDefault(item.getId(), 0L));
    }

    private static AddonResponse toResponse(Addon addon, Map<UUID, List<String>> dishes) {
        return new AddonResponse(addon.getId().toString(), addon.getName(), addon.getPrice(),
                dishes.getOrDefault(addon.getId(), List.of()));
    }
}
