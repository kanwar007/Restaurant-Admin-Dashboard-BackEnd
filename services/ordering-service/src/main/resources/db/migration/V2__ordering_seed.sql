CREATE OR REPLACE FUNCTION ordering.seed_order(p_number bigint, p_table text, p_status text, p_source text,
                                               p_notes text, p_placed timestamptz, p_items jsonb) RETURNS void AS $$
DECLARE
    tenant    uuid := '11111111-1111-1111-1111-111111111111';
    order_id  uuid := gen_random_uuid();
    item      jsonb;
    addon     jsonb;
    item_id   uuid;
    line_no   integer := 0;
BEGIN
    INSERT INTO ordering.orders (id, restaurant_id, order_number, table_number_snapshot, status, source, notes,
                                 placed_at, served_at, cancelled_at, created_at, updated_at)
    VALUES (order_id, tenant, p_number, p_table, p_status, p_source, p_notes, p_placed,
            CASE WHEN p_status IN ('served', 'completed') THEN p_placed + interval '20 minutes' END,
            CASE WHEN p_status = 'cancelled' THEN p_placed + interval '5 minutes' END,
            p_placed, p_placed);

    INSERT INTO ordering.order_status_history (order_id, from_status, to_status, created_at)
    VALUES (order_id, NULL, p_status, p_placed);

    FOR item IN SELECT * FROM jsonb_array_elements(p_items)
        LOOP
            line_no := line_no + 1;
            item_id := gen_random_uuid();
            INSERT INTO ordering.order_items (id, order_id, item_name_snapshot, unit_price_snapshot, quantity,
                                              line_total, line_no, created_at)
            VALUES (item_id, order_id, item ->> 'name', (item ->> 'price')::numeric, (item ->> 'qty')::int,
                    ((item ->> 'price')::numeric
                        + coalesce((SELECT sum((value ->> 'price')::numeric)
                                    FROM jsonb_array_elements(coalesce(item -> 'addons', '[]'::jsonb))), 0))
                        * (item ->> 'qty')::int,
                    line_no, p_placed);

            FOR addon IN SELECT * FROM jsonb_array_elements(coalesce(item -> 'addons', '[]'::jsonb))
                LOOP
                    INSERT INTO ordering.order_item_addons (order_item_id, addon_name_snapshot, unit_price_snapshot,
                                                            quantity, created_at)
                    VALUES (item_id, addon ->> 'name', (addon ->> 'price')::numeric, (item ->> 'qty')::int, p_placed);
                END LOOP;
        END LOOP;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ordering.seed_demo_data() RETURNS void AS $$
DECLARE
    tenant uuid := '11111111-1111-1111-1111-111111111111';
BEGIN
    TRUNCATE ordering.order_item_addons, ordering.order_items, ordering.order_status_history, ordering.orders,
        ordering.order_number_allocations CASCADE;

    -- Active service, relative to the current time so the dashboard always looks live.
    PERFORM ordering.seed_order(1, 'T-05', 'new', 'staff', 'Less sugar', now() - interval '5 minutes',
                                '[{"name":"Cappuccino","qty":2,"price":150,"addons":[{"name":"Extra Shot","price":30},{"name":"Almond Milk","price":40}]},
                                  {"name":"Croissant","qty":1,"price":80}]');
    PERFORM ordering.seed_order(2, 'T-12', 'new', 'staff', NULL, now() - interval '12 minutes',
                                '[{"name":"Latte","qty":1,"price":160,"addons":[{"name":"Caramel Syrup","price":25}]},
                                  {"name":"Caesar Salad","qty":2,"price":280}]');
    PERFORM ordering.seed_order(3, 'T-03', 'new', 'staff', NULL, now() - interval '20 minutes',
                                '[{"name":"Espresso","qty":3,"price":120},{"name":"Croissant","qty":3,"price":80}]');
    PERFORM ordering.seed_order(4, 'T-18', 'new', 'staff', NULL, now() - interval '25 minutes',
                                '[{"name":"Cappuccino","qty":2,"price":150},{"name":"Caesar Salad","qty":1,"price":280}]');
    PERFORM ordering.seed_order(5, 'T-22', 'kot-printed', 'staff', NULL, now() - interval '10 minutes',
                                '[{"name":"Latte","qty":1,"price":160},{"name":"Blueberry Muffin","qty":2,"price":90}]');
    PERFORM ordering.seed_order(6, 'T-14', 'kot-printed', 'staff', NULL, now() - interval '15 minutes',
                                '[{"name":"Espresso","qty":2,"price":120},{"name":"Croissant","qty":2,"price":80}]');
    PERFORM ordering.seed_order(7, 'T-09', 'kot-printed', 'staff', NULL, now() - interval '22 minutes',
                                '[{"name":"Caesar Salad","qty":1,"price":280},{"name":"Cappuccino","qty":1,"price":150}]');
    PERFORM ordering.seed_order(8, 'T-21', 'served', 'staff', NULL, now() - interval '35 minutes',
                                '[{"name":"Latte","qty":2,"price":160},{"name":"Croissant","qty":2,"price":80}]');
    PERFORM ordering.seed_order(9, 'T-15', 'served', 'guest', 'Table service', now() - interval '40 minutes',
                                '[{"name":"Cappuccino","qty":1,"price":150}]');

    -- Closed business used by order history and billing.
    PERFORM ordering.seed_order(41, 'T-07', 'completed', 'staff', NULL, now() - interval '1 day 3 hours',
                                '[{"name":"Espresso","qty":2,"price":120},{"name":"Croissant","qty":2,"price":80}]');
    PERFORM ordering.seed_order(42, 'T-14', 'completed', 'staff', NULL, now() - interval '1 day 2 hours',
                                '[{"name":"Latte","qty":2,"price":160},{"name":"Caesar Salad","qty":1,"price":280}]');
    PERFORM ordering.seed_order(43, 'T-22', 'completed', 'staff', NULL, now() - interval '1 day 1 hour',
                                '[{"name":"Caesar Salad","qty":3,"price":280},{"name":"Cappuccino","qty":3,"price":150}]');
    PERFORM ordering.seed_order(44, 'T-09', 'completed', 'staff', NULL, now() - interval '4 hours',
                                '[{"name":"Cappuccino","qty":2,"price":150},{"name":"Blueberry Muffin","qty":2,"price":90}]');
    PERFORM ordering.seed_order(45, 'T-03', 'cancelled', 'guest', 'Guest left', now() - interval '3 hours',
                                '[{"name":"Espresso","qty":3,"price":120}]');
    PERFORM ordering.seed_order(46, 'T-18', 'completed', 'staff', NULL, now() - interval '2 hours 30 minutes',
                                '[{"name":"Caesar Salad","qty":2,"price":280},{"name":"Latte","qty":3,"price":160},
                                  {"name":"Croissant","qty":2,"price":80}]');
    PERFORM ordering.seed_order(47, 'T-05', 'completed', 'staff', NULL, now() - interval '2 hours',
                                '[{"name":"Espresso","qty":1,"price":120},{"name":"Croissant","qty":1,"price":80}]');
    PERFORM ordering.seed_order(48, 'T-12', 'completed', 'staff', NULL, now() - interval '1 hour 30 minutes',
                                '[{"name":"Latte","qty":2,"price":160,"addons":[{"name":"Oat Milk","price":45}]},
                                  {"name":"Caesar Salad","qty":1,"price":280},{"name":"Croissant","qty":2,"price":80}]');

    INSERT INTO ordering.order_number_allocations (restaurant_id, next_number) VALUES (tenant, 49);
END;
$$ LANGUAGE plpgsql;

SELECT ordering.seed_demo_data();
