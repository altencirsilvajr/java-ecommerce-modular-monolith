CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS inventory;
CREATE SCHEMA IF NOT EXISTS orders;
CREATE SCHEMA IF NOT EXISTS payments;
CREATE SCHEMA IF NOT EXISTS shared;

CREATE TABLE users.user_accounts (
  id UUID PRIMARY KEY, email VARCHAR(255) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, role VARCHAR(32) NOT NULL
);
CREATE TABLE catalog.products (
  id UUID PRIMARY KEY, sku VARCHAR(255) NOT NULL UNIQUE, name VARCHAR(255) NOT NULL,
  price NUMERIC(19,2) NOT NULL CHECK (price > 0), active BOOLEAN NOT NULL
);
CREATE TABLE inventory.stock_items (
  product_id UUID PRIMARY KEY, available INTEGER NOT NULL CHECK (available >= 0),
  reserved INTEGER NOT NULL CHECK (reserved >= 0), version BIGINT NOT NULL
);
CREATE TABLE inventory.adjustment_requests (
  idempotency_key VARCHAR(255) PRIMARY KEY, payload_hash VARCHAR(64) NOT NULL, product_id UUID NOT NULL
);
CREATE TABLE orders.purchase_orders (
  id UUID PRIMARY KEY, customer_id UUID NOT NULL, product_id UUID NOT NULL, product_name VARCHAR(255) NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity > 0), total NUMERIC(19,2) NOT NULL,
  status VARCHAR(32) NOT NULL, failure_reason VARCHAR(255), created_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL
);
CREATE INDEX ix_orders_customer_created ON orders.purchase_orders(customer_id, created_at DESC);
CREATE TABLE orders.idempotency_requests (
  idempotency_key VARCHAR(255) PRIMARY KEY, payload_hash VARCHAR(64) NOT NULL, order_id UUID NOT NULL UNIQUE
);
CREATE TABLE payments.payments (
  id UUID PRIMARY KEY, order_id UUID NOT NULL UNIQUE, amount NUMERIC(19,2) NOT NULL,
  status VARCHAR(32) NOT NULL, processed_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE shared.outbox_messages (
  id UUID PRIMARY KEY, event_type VARCHAR(255) NOT NULL, payload VARCHAR(10000) NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL, published_at TIMESTAMPTZ
);
CREATE INDEX ix_outbox_pending ON shared.outbox_messages(occurred_at) WHERE published_at IS NULL;
CREATE TABLE shared.inbox_messages (
  id VARCHAR(512) PRIMARY KEY, processed_at TIMESTAMPTZ NOT NULL
);
