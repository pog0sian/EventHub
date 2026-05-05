CREATE TABLE reward_purchases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    point_transaction_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reward_purchases_user
      FOREIGN KEY (user_id)
          REFERENCES users (id)
          ON DELETE CASCADE,

    CONSTRAINT fk_reward_purchases_reward
      FOREIGN KEY (reward_id)
          REFERENCES rewards (id)
          ON DELETE RESTRICT,

    CONSTRAINT fk_reward_purchases_point_transaction
      FOREIGN KEY (point_transaction_id)
          REFERENCES point_transactions (id)
          ON DELETE RESTRICT
);
