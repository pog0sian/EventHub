CREATE TABLE organization_managers (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_organization_managers_organization
       FOREIGN KEY (organization_id)
           REFERENCES organizations (id)
           ON DELETE CASCADE,

    CONSTRAINT fk_organization_managers_user
       FOREIGN KEY (user_id)
           REFERENCES users (id)
           ON DELETE CASCADE,

    CONSTRAINT uq_organization_managers_organization_user
       UNIQUE (organization_id, user_id)
);
