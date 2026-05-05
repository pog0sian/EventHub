# EventHub ER Diagram

Документ описывает текущую ER-модель backend-приложения EventHub по JPA-сущностям и Flyway-миграциям.

## Диаграмма

```mermaid
erDiagram
    users {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar first_name
        varchar last_name
        varchar patronymic
        boolean enabled
        timestamptz created_at
        timestamptz updated_at
    }

    roles {
        bigint id PK
        varchar name UK
        varchar description
        timestamptz created_at
        timestamptz updated_at
    }

    user_roles {
        bigint id PK
        bigint user_id FK
        bigint role_id FK
        timestamptz created_at
    }

    organizations {
        bigint id PK
        varchar name UK
        text description
        varchar contact_email
        boolean active
        timestamptz created_at
        timestamptz updated_at
    }

    organization_managers {
        bigint id PK
        bigint organization_id FK
        bigint user_id FK
        boolean active
        timestamptz created_at
        timestamptz updated_at
    }

    events {
        bigint id PK
        bigint organization_id FK
        varchar title
        text description
        varchar location
        timestamptz starts_at
        timestamptz ends_at
        integer points_reward
        integer capacity
        varchar status
        timestamptz created_at
        timestamptz updated_at
    }

    event_registrations {
        bigint id PK
        bigint event_id FK
        bigint user_id FK
        varchar status
        timestamptz created_at
        timestamptz updated_at
    }

    attendance {
        bigint id PK
        bigint event_id FK
        bigint user_id FK
        boolean attended
        bigint marked_by_user_id FK
        timestamptz marked_at
        timestamptz created_at
        timestamptz updated_at
    }

    point_transactions {
        bigint id PK
        bigint user_id FK
        bigint event_id FK
        integer amount
        varchar type
        varchar description
        timestamptz created_at
    }

    rewards {
        bigint id PK
        varchar title
        text description
        integer cost
        integer stock
        boolean active
        timestamptz created_at
        timestamptz updated_at
    }

    reward_purchases {
        bigint id PK
        bigint user_id FK
        bigint reward_id FK
        bigint point_transaction_id FK
        varchar status
        timestamptz created_at
        timestamptz updated_at
    }

    users ||--o{ user_roles : has
    roles ||--o{ user_roles : grants
    organizations ||--o{ organization_managers : has
    users ||--o{ organization_managers : manages
    organizations ||--o{ events : owns
    events ||--o{ event_registrations : has
    users ||--o{ event_registrations : registers
    events ||--o{ attendance : tracks
    users ||--o{ attendance : attends
    users ||--o{ attendance : marks
    users ||--o{ point_transactions : owns
    events |o--o{ point_transactions : source
    rewards ||--o{ reward_purchases : purchased
    users ||--o{ reward_purchases : buys
    point_transactions ||--o{ reward_purchases : pays_for
