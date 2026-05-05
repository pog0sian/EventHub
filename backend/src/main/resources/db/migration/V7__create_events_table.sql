CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    points_reward INTEGER NOT NULL,
    capacity INTEGER,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_events_organization
        FOREIGN KEY (organization_id)
            REFERENCES organizations (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_events_points_reward_non_negative
        CHECK (points_reward >= 0),

    CONSTRAINT chk_events_capacity_positive
        CHECK (capacity IS NULL OR capacity > 0),

    CONSTRAINT chk_events_dates_order
        CHECK (ends_at > starts_at)
);
