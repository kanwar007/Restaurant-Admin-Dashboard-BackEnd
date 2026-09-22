CREATE OR REPLACE FUNCTION billing.seed_invoice(p_order_no text, p_table text, p_subtotal numeric, p_mode text,
                                                p_issued timestamptz, p_lines jsonb) RETURNS void AS $$
DECLARE
    tenant     uuid := '11111111-1111-1111-1111-111111111111';
    tax_rate   numeric := 0.0500;
    tax_total  numeric := round(p_subtotal * 0.0500, 2);
    invoice_id uuid := gen_random_uuid();
    line       jsonb;
    line_no    integer := 0;
BEGIN
    INSERT INTO billing.invoices (id, restaurant_id, order_no, table_number, subtotal, tax_rate, tax_total, total,
                                  payment_mode, status, issued_at, created_at, updated_at)
    VALUES (invoice_id, tenant, p_order_no, p_table, p_subtotal, tax_rate, tax_total, p_subtotal + tax_total,
            p_mode, 'paid', p_issued, p_issued, p_issued);

    FOR line IN SELECT * FROM jsonb_array_elements(p_lines)
        LOOP
            line_no := line_no + 1;
            INSERT INTO billing.invoice_lines (invoice_id, description, quantity, unit_price, amount, line_no)
            VALUES (invoice_id, line ->> 'name', (line ->> 'qty')::int, (line ->> 'price')::numeric,
                    (line ->> 'amount')::numeric, line_no);
        END LOOP;

    INSERT INTO billing.payments (invoice_id, payment_mode, amount, captured_at)
    VALUES (invoice_id, p_mode, p_subtotal + tax_total, p_issued);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION billing.seed_demo_data() RETURNS void AS $$
BEGIN
    TRUNCATE billing.payments, billing.invoice_lines, billing.invoices CASCADE;

    PERFORM billing.seed_invoice('#041', 'T-07', 400, 'upi', now() - interval '1 day 2 hours 40 minutes',
                                 '[{"name":"Espresso","qty":2,"price":120,"amount":240},
                                   {"name":"Croissant","qty":2,"price":80,"amount":160}]');
    PERFORM billing.seed_invoice('#042', 'T-14', 600, 'cash', now() - interval '1 day 1 hour 40 minutes',
                                 '[{"name":"Latte","qty":2,"price":160,"amount":320},
                                   {"name":"Caesar Salad","qty":1,"price":280,"amount":280}]');
    PERFORM billing.seed_invoice('#043', 'T-22', 1290, 'card', now() - interval '1 day 40 minutes',
                                 '[{"name":"Caesar Salad","qty":3,"price":280,"amount":840},
                                   {"name":"Cappuccino","qty":3,"price":150,"amount":450}]');
    PERFORM billing.seed_invoice('#044', 'T-09', 480, 'upi', now() - interval '3 hours 40 minutes',
                                 '[{"name":"Cappuccino","qty":2,"price":150,"amount":300},
                                   {"name":"Blueberry Muffin","qty":2,"price":90,"amount":180}]');
    PERFORM billing.seed_invoice('#046', 'T-18', 1200, 'card', now() - interval '2 hours 10 minutes',
                                 '[{"name":"Caesar Salad","qty":2,"price":280,"amount":560},
                                   {"name":"Latte","qty":3,"price":160,"amount":480},
                                   {"name":"Croissant","qty":2,"price":80,"amount":160}]');
    PERFORM billing.seed_invoice('#047', 'T-05', 200, 'cash', now() - interval '1 hour 40 minutes',
                                 '[{"name":"Espresso","qty":1,"price":120,"amount":120},
                                   {"name":"Croissant","qty":1,"price":80,"amount":80}]');
    PERFORM billing.seed_invoice('#048', 'T-12', 850, 'upi', now() - interval '1 hour 10 minutes',
                                 '[{"name":"Latte","qty":2,"price":205,"amount":410},
                                   {"name":"Caesar Salad","qty":1,"price":280,"amount":280},
                                   {"name":"Croissant","qty":2,"price":80,"amount":160}]');
END;
$$ LANGUAGE plpgsql;

SELECT billing.seed_demo_data();
