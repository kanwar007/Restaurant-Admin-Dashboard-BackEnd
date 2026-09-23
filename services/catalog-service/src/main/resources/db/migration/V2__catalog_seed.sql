CREATE OR REPLACE FUNCTION catalog.seed_demo_data() RETURNS void AS $$
DECLARE
    tenant uuid := '11111111-1111-1111-1111-111111111111';
BEGIN
    TRUNCATE catalog.menu_item_addons, catalog.menu_items, catalog.addons, catalog.categories CASCADE;

    INSERT INTO catalog.categories (id, restaurant_id, name, sort_order)
    VALUES ('33333333-0000-0000-0000-000000000001', tenant, 'Coffee', 1),
           ('33333333-0000-0000-0000-000000000002', tenant, 'Bakery', 2),
           ('33333333-0000-0000-0000-000000000003', tenant, 'Salads', 3),
           ('33333333-0000-0000-0000-000000000004', tenant, 'Sandwiches', 4),
           ('33333333-0000-0000-0000-000000000005', tenant, 'Beverages', 5);

    INSERT INTO catalog.menu_items (id, restaurant_id, category_id, name, price, is_available, sort_order)
    VALUES ('44444444-0000-0000-0000-000000000001', tenant, '33333333-0000-0000-0000-000000000001', 'Espresso', 120.00, true, 1),
           ('44444444-0000-0000-0000-000000000002', tenant, '33333333-0000-0000-0000-000000000001', 'Cappuccino', 150.00, true, 2),
           ('44444444-0000-0000-0000-000000000003', tenant, '33333333-0000-0000-0000-000000000001', 'Latte', 160.00, true, 3),
           ('44444444-0000-0000-0000-000000000004', tenant, '33333333-0000-0000-0000-000000000002', 'Croissant', 80.00, true, 1),
           ('44444444-0000-0000-0000-000000000005', tenant, '33333333-0000-0000-0000-000000000002', 'Blueberry Muffin', 90.00, false, 2),
           ('44444444-0000-0000-0000-000000000006', tenant, '33333333-0000-0000-0000-000000000003', 'Caesar Salad', 280.00, true, 1);

    INSERT INTO catalog.addons (id, restaurant_id, name, price)
    VALUES ('55555555-0000-0000-0000-000000000001', tenant, 'Extra Shot', 30.00),
           ('55555555-0000-0000-0000-000000000002', tenant, 'Almond Milk', 40.00),
           ('55555555-0000-0000-0000-000000000003', tenant, 'Caramel Syrup', 25.00),
           ('55555555-0000-0000-0000-000000000004', tenant, 'Whipped Cream', 35.00),
           ('55555555-0000-0000-0000-000000000005', tenant, 'Vanilla Extract', 20.00),
           ('55555555-0000-0000-0000-000000000006', tenant, 'Cinnamon Powder', 15.00),
           ('55555555-0000-0000-0000-000000000007', tenant, 'Chocolate Drizzle', 30.00),
           ('55555555-0000-0000-0000-000000000008', tenant, 'Oat Milk', 45.00);

    INSERT INTO catalog.menu_item_addons (menu_item_id, addon_id)
    SELECT item.id, addon.id
    FROM catalog.menu_items item
             JOIN catalog.addons addon ON true
    WHERE (item.name, addon.name) IN (
        ('Espresso', 'Extra Shot'), ('Espresso', 'Cinnamon Powder'),
        ('Cappuccino', 'Extra Shot'), ('Cappuccino', 'Almond Milk'), ('Cappuccino', 'Caramel Syrup'),
        ('Cappuccino', 'Whipped Cream'), ('Cappuccino', 'Cinnamon Powder'), ('Cappuccino', 'Chocolate Drizzle'),
        ('Cappuccino', 'Oat Milk'),
        ('Latte', 'Extra Shot'), ('Latte', 'Almond Milk'), ('Latte', 'Caramel Syrup'), ('Latte', 'Whipped Cream'),
        ('Latte', 'Vanilla Extract'), ('Latte', 'Chocolate Drizzle'), ('Latte', 'Oat Milk'),
        ('Croissant', 'Whipped Cream'), ('Croissant', 'Chocolate Drizzle'),
        ('Blueberry Muffin', 'Whipped Cream')
    );
END;
$$ LANGUAGE plpgsql;

SELECT catalog.seed_demo_data();
