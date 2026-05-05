CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    attended BOOLEAN NOT NULL,
    marked_by_user_id BIGINT NOT NULL,
    marked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attendance_event
        FOREIGN KEY (event_id)
            REFERENCES events (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_attendance_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_attendance_marked_by_user
        FOREIGN KEY (marked_by_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT uq_attendance_event_user
        UNIQUE (event_id, user_id)
);
