CREATE OR REPLACE FUNCTION floor.seed_demo_data() RETURNS void AS $$
DECLARE
    tenant uuid := '11111111-1111-1111-1111-111111111111';
BEGIN
    TRUNCATE floor.dining_tables CASCADE;

    INSERT INTO floor.dining_tables (restaurant_id, number, capacity, status)
    SELECT tenant,
           'T-' || lpad(seat::text, 2, '0'),
           CASE WHEN seat % 4 = 0 THEN 6 WHEN seat % 3 = 0 THEN 4 ELSE 2 END,
           'vacant'
    FROM generate_series(1, 24) AS seat;

    UPDATE floor.dining_tables SET status = 'reserved' WHERE number IN ('T-06', 'T-10');

    UPDATE floor.dining_tables AS t
    SET status = occupancy.status,
        current_order_display = occupancy.order_no
    FROM (VALUES ('T-05', 'occupied', '#001'),
                 ('T-12', 'bill-pending', '#002'),
                 ('T-03', 'bill-pending', '#003'),
                 ('T-18', 'occupied', '#004'),
                 ('T-22', 'occupied', '#005'),
                 ('T-14', 'occupied', '#006'),
                 ('T-09', 'occupied', '#007'),
                 ('T-21', 'occupied', '#008'),
                 ('T-15', 'occupied', '#009')) AS occupancy(number, status, order_no)
    WHERE t.number = occupancy.number;
END;
$$ LANGUAGE plpgsql;

SELECT floor.seed_demo_data();
