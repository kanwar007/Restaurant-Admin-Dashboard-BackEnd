CREATE EXTENSION IF NOT EXISTS citext;

CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE identity.restaurants (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code        citext NOT NULL UNIQUE,
    name        text NOT NULL,
    tagline     text NOT NULL DEFAULT '',
    gstin       text NOT NULL DEFAULT '',
    address     text NOT NULL,
    phone       text NOT NULL,
    timezone    text NOT NULL DEFAULT 'UTC',
    currency    char(3) NOT NULL DEFAULT 'INR',
    tax_rate    numeric(5, 4) NOT NULL DEFAULT 0.0500,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT restaurants_tax_rate_range CHECK (tax_rate >= 0 AND tax_rate <= 1)
);

CREATE TABLE identity.staff_users (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id  uuid NOT NULL REFERENCES identity.restaurants (id) ON DELETE CASCADE,
    username       citext NOT NULL,
    password_hash  text NOT NULL,
    name           text NOT NULL,
    role           text NOT NULL,
    initials       varchar(8) NOT NULL,
    is_active      boolean NOT NULL DEFAULT true,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT staff_users_username_unique UNIQUE (restaurant_id, username)
);

CREATE TABLE identity.user_sessions (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       uuid NOT NULL REFERENCES identity.staff_users (id) ON DELETE CASCADE,
    token_hash    bytea NOT NULL UNIQUE,
    expires_at    timestamptz NOT NULL,
    revoked_at    timestamptz,
    created_at    timestamptz NOT NULL DEFAULT now(),
    last_seen_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX user_sessions_active_idx ON identity.user_sessions (token_hash, expires_at)
    WHERE revoked_at IS NULL;
