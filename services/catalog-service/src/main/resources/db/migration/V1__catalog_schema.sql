CREATE SCHEMA IF NOT EXISTS catalog;

CREATE TABLE catalog.categories (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id  uuid NOT NULL,
    name           text NOT NULL,
    sort_order     integer NOT NULL DEFAULT 0,
    is_active      boolean NOT NULL DEFAULT true,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT categories_name_unique UNIQUE (restaurant_id, name)
);

CREATE TABLE catalog.menu_items (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id  uuid NOT NULL,
    category_id    uuid NOT NULL REFERENCES catalog.categories (id) ON DELETE RESTRICT,
    name           text NOT NULL,
    price          numeric(12, 2) NOT NULL,
    is_available   boolean NOT NULL DEFAULT true,
    sort_order     integer NOT NULL DEFAULT 0,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    deleted_at     timestamptz,
    CONSTRAINT menu_items_price_positive CHECK (price >= 0)
);

CREATE INDEX menu_items_lookup_idx ON catalog.menu_items (restaurant_id, category_id, is_available);

CREATE TABLE catalog.addons (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id  uuid NOT NULL,
    name           text NOT NULL,
    price          numeric(12, 2) NOT NULL,
    is_active      boolean NOT NULL DEFAULT true,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT addons_price_positive CHECK (price >= 0),
    CONSTRAINT addons_name_unique UNIQUE (restaurant_id, name)
);

CREATE TABLE catalog.menu_item_addons (
    menu_item_id  uuid NOT NULL REFERENCES catalog.menu_items (id) ON DELETE CASCADE,
    addon_id      uuid NOT NULL REFERENCES catalog.addons (id) ON DELETE CASCADE,
    created_at    timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (menu_item_id, addon_id)
);
