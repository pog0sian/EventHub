CREATE TABLE point_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT,
    amount INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_point_transactions_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_point_transactions_event
        FOREIGN KEY (event_id)
            REFERENCES events (id)
            ON DELETE SET NULL,

    CONSTRAINT chk_point_transactions_amount_not_zero
        CHECK (amount <> 0)
);
