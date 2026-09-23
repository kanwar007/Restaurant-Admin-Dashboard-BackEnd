CREATE SCHEMA IF NOT EXISTS floor;

CREATE TABLE floor.dining_tables (
    id                     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id          uuid NOT NULL,
    number                 text NOT NULL,
    capacity               integer NOT NULL,
    status                 text NOT NULL DEFAULT 'vacant',
    current_order_id       uuid,
    current_order_display  text,
    created_at             timestamptz NOT NULL DEFAULT now(),
    updated_at             timestamptz NOT NULL DEFAULT now(),
    version                integer NOT NULL DEFAULT 1,
    CONSTRAINT dining_tables_capacity_positive CHECK (capacity > 0),
    CONSTRAINT dining_tables_status_allowed CHECK (status IN ('vacant', 'occupied', 'reserved', 'bill-pending')),
    CONSTRAINT dining_tables_number_unique UNIQUE (restaurant_id, number)
);
