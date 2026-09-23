CREATE SCHEMA IF NOT EXISTS billing;

CREATE TABLE billing.invoices (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   uuid NOT NULL,
    order_id        uuid,
    order_no        text NOT NULL,
    table_number    text NOT NULL,
    subtotal        numeric(12, 2) NOT NULL,
    tax_rate        numeric(6, 4) NOT NULL,
    tax_total       numeric(12, 2) NOT NULL,
    total           numeric(12, 2) NOT NULL,
    payment_mode    text NOT NULL,
    status          text NOT NULL DEFAULT 'paid',
    issued_at       timestamptz NOT NULL DEFAULT now(),
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    version         integer NOT NULL DEFAULT 1,
    CONSTRAINT invoices_payment_mode_allowed CHECK (payment_mode IN ('cash', 'card', 'upi')),
    CONSTRAINT invoices_status_allowed CHECK (status IN ('draft', 'paid', 'void')),
    CONSTRAINT invoices_order_no_unique UNIQUE (restaurant_id, order_no)
);

CREATE INDEX invoices_issued_at_idx ON billing.invoices (restaurant_id, issued_at DESC);

CREATE TABLE billing.invoice_lines (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id   uuid NOT NULL REFERENCES billing.invoices (id) ON DELETE CASCADE,
    description  text NOT NULL,
    quantity     integer NOT NULL,
    unit_price   numeric(12, 2) NOT NULL,
    amount       numeric(12, 2) NOT NULL,
    line_no      integer NOT NULL DEFAULT 0,
    CONSTRAINT invoice_lines_quantity_positive CHECK (quantity > 0)
);

CREATE TABLE billing.payments (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id    uuid NOT NULL REFERENCES billing.invoices (id) ON DELETE CASCADE,
    payment_mode  text NOT NULL,
    amount        numeric(12, 2) NOT NULL,
    reference     text,
    captured_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT payments_mode_allowed CHECK (payment_mode IN ('cash', 'card', 'upi'))
);
