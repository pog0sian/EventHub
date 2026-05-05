CREATE TABLE event_registrations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_registrations_event
     FOREIGN KEY (event_id)
         REFERENCES events (id)
         ON DELETE CASCADE,

    CONSTRAINT fk_event_registrations_user
     FOREIGN KEY (user_id)
         REFERENCES users (id)
         ON DELETE CASCADE,

    CONSTRAINT uq_event_registrations_event_user
     UNIQUE (event_id, user_id)
);
