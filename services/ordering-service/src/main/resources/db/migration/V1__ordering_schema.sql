CREATE SCHEMA IF NOT EXISTS ordering;

CREATE TABLE ordering.order_number_allocations (
    restaurant_id  uuid PRIMARY KEY,
    next_number    bigint NOT NULL DEFAULT 1
);

CREATE TABLE ordering.orders (
    id                     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id          uuid NOT NULL,
    order_number           bigint NOT NULL,
    table_id               uuid,
    table_number_snapshot  text NOT NULL,
    status                 text NOT NULL DEFAULT 'new',
    source                 text NOT NULL DEFAULT 'staff',
    customer_name          text,
    notes                  text,
    placed_at              timestamptz NOT NULL DEFAULT now(),
    served_at              timestamptz,
    cancelled_at           timestamptz,
    created_by             uuid,
    created_at             timestamptz NOT NULL DEFAULT now(),
    updated_at             timestamptz NOT NULL DEFAULT now(),
    version                integer NOT NULL DEFAULT 1,
    CONSTRAINT orders_status_allowed CHECK (status IN ('new', 'kot-printed', 'served', 'completed', 'cancelled')),
    CONSTRAINT orders_source_allowed CHECK (source IN ('staff', 'guest')),
    CONSTRAINT orders_number_unique UNIQUE (restaurant_id, order_number)
);

CREATE INDEX orders_status_idx ON ordering.orders (restaurant_id, status, placed_at DESC);

CREATE TABLE ordering.order_items (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id             uuid NOT NULL REFERENCES ordering.orders (id) ON DELETE CASCADE,
    menu_item_id         uuid,
    item_name_snapshot   text NOT NULL,
    unit_price_snapshot  numeric(12, 2) NOT NULL,
    quantity             integer NOT NULL,
    line_total           numeric(12, 2) NOT NULL,
    notes                text,
    line_no              integer NOT NULL DEFAULT 0,
    created_at           timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT order_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT order_items_unit_price_positive CHECK (unit_price_snapshot >= 0),
    CONSTRAINT order_items_line_total_positive CHECK (line_total >= 0)
);

CREATE TABLE ordering.order_item_addons (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id        uuid NOT NULL REFERENCES ordering.order_items (id) ON DELETE CASCADE,
    addon_id             uuid,
    addon_name_snapshot  text NOT NULL,
    unit_price_snapshot  numeric(12, 2) NOT NULL,
    quantity             integer NOT NULL DEFAULT 1,
    created_at           timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT order_item_addons_quantity_positive CHECK (quantity > 0)
);

CREATE TABLE ordering.order_status_history (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     uuid NOT NULL REFERENCES ordering.orders (id) ON DELETE CASCADE,
    from_status  text,
    to_status    text NOT NULL,
    changed_by   uuid,
    reason       text,
    created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX order_status_history_order_idx ON ordering.order_status_history (order_id, created_at);
