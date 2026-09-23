-- Demo data is created by a function so that the development-only reset endpoint
-- can restore exactly the same state the migration created.
CREATE OR REPLACE FUNCTION identity.seed_demo_data() RETURNS void AS $$
BEGIN
    TRUNCATE identity.user_sessions, identity.staff_users, identity.restaurants CASCADE;

    INSERT INTO identity.restaurants (id, code, name, tagline, gstin, address, phone, timezone, currency, tax_rate)
    VALUES ('11111111-1111-1111-1111-111111111111', 'CAFE01', 'Café Admin', 'Restaurant Management',
            '29ABCDE1234F1Z5', '12 Brew Street, Indiranagar, Bengaluru 560038', '+91 80 4123 7788',
            'Asia/Kolkata', 'INR', 0.0500);

    INSERT INTO identity.staff_users (id, restaurant_id, username, password_hash, name, role, initials)
    VALUES ('22222222-2222-2222-2222-222222222221', '11111111-1111-1111-1111-111111111111', 'admin',
            '$2b$10$Kne7aYfckptJIq4zNcWin.Sof732geacCQMuG5hqSUCGHlsURhhs.', 'Admin User', 'Manager', 'AD'),
           ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'cashier',
            '$2b$10$ZdTEJEt8Fx6dIR/vzuwb6u/K.YoynWZE5kYd5s8KcLAnbNm5xwWU2', 'Riya Sharma', 'Cashier', 'RS');
END;
$$ LANGUAGE plpgsql;

SELECT identity.seed_demo_data();
