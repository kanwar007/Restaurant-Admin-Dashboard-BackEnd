package com.cafeadmin.catalog.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cafeadmin.catalog.domain.MenuItemAddon;

public interface MenuItemAddonRepository extends JpaRepository<MenuItemAddon, MenuItemAddon.Key> {

    @Query("""
            select link.menuItemId, count(link)
            from MenuItemAddon link
            group by link.menuItemId
            """)
    List<Object[]> countByMenuItem();

    @Query("""
            select link.addonId, item.name
            from MenuItemAddon link
            join MenuItem item on item.id = link.menuItemId
            where item.deletedAt is null
            order by item.name asc
            """)
    List<Object[]> linkedDishNames();

    void deleteByAddonId(UUID addonId);

    void deleteByMenuItemId(UUID menuItemId);
}
