CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS alert_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    home_airport CHAR(3) NOT NULL,
    max_budget INTEGER,
    nonstop_only BOOLEAN NOT NULL DEFAULT FALSE,
    regions TEXT
);

CREATE TYPE deal_source AS ENUM ('KIWI', 'TRAVELPAYOUTS');

CREATE TABLE IF NOT EXISTS deals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    origin CHAR(3) NOT NULL,
    destination CHAR(3) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    currency TEXT NOT NULL,
    depart_at TIMESTAMPTZ,
    return_at TIMESTAMPTZ,
    deep_link TEXT,
    source deal_source NOT NULL,
    is_error_fare BOOLEAN NOT NULL,
    score NUMERIC(6,2) NOT NULL,
    found_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ,
    raw_payload JSONB,
    price_per_km NUMERIC(10,4)
);

CREATE INDEX IF NOT EXISTS idx_deal_origin_destination_found_at
    ON deals (origin, destination, found_at DESC);

CREATE TABLE IF NOT EXISTS price_samples (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    origin CHAR(3) NOT NULL,
    destination CHAR(3) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    currency TEXT NOT NULL,
    collected_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_price_sample_origin_destination_collected_at
    ON price_samples (origin, destination, collected_at DESC);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_unique ON users (lower(email));
