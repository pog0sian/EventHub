# Соответствие требованиям курсовой работы

## Тема проекта

EventHub - веб-приложение для университетской экосистемы мероприятий.

Система позволяет организациям публиковать мероприятия, студентам записываться и посещать их, менеджерам отмечать посещаемость, а администратору управлять пользователями, организациями, менеджерами и наградами. За посещение мероприятий студентам начисляются баллы, которые можно обменивать на награды.

## Таблица соответствия требованиям темы

| Требование | Где реализовано | Endpoint / сценарий |
| --- | --- | --- |
| Минимум 2 роли с разными правами | Роли `STUDENT`, `ORG_MANAGER`, `ADMIN`; проверка активной роли через `X-Active-Role` | Студент записывается на мероприятия, менеджер управляет мероприятиями своей организации, администратор управляет организациями и наградами |
| Минимум 5 сущностей и связи между ними | `UserEntity`, `RoleEntity`, `OrganizationEntity`, `OrganizationManagerEntity`, `EventEntity`, `EventRegistrationEntity`, `AttendanceEntity`, `PointTransactionEntity`, `RewardEntity`, `RewardPurchaseEntity` | ER-модель описана в `docs/ER.md` |
| Транзакционный сценарий | Запись на мероприятие, покупка награды, списание баллов | Защита от переполнения мероприятия, отрицательного баланса и превышения остатка наград через транзакции и pessimistic lock |
| Кэшируемый сценарий | `RewardService.getActiveRewards()` | Список активных наград кэшируется через Redis и `@Cacheable`; кэш сбрасывается при изменении наград и покупке |
| Фоновый сценарий | `EventStatusScheduler` | `@Scheduled` автоматически завершает опубликованные мероприятия, у которых `endsAt` уже прошел |
| Безопасность | Spring Security, JWT, роли, active role guard | JWT-фильтр аутентифицирует пользователя, `ActiveRoleFilter` проверяет активную роль |
| Тестируемость | Testcontainers PostgreSQL | Интеграционные тесты auth, ролей, записи на мероприятие, посещаемости и покупки награды |
| Наблюдаемость | Spring Actuator | `/actuator/health`, `/actuator/metrics` |
| Frontend-покрытие | Vue 3 + TypeScript страницы по ролям | Студент, менеджер и администратор имеют отдельные UI-сценарии |

## Таблица соответствия обязательному стеку

| Требование стека | Где реализовано | Комментарий |
| --- | --- | --- |
| Kotlin + Spring Boot | `backend/pom.xml`, `backend/src/main/kotlin/ru/eventhub` | Backend написан на Kotlin и Spring Boot |
| Vue 3 + TypeScript | `frontend/package.json`, `frontend/src` | Frontend написан на Vue 3 и TypeScript |
| Controller -> Service -> Repository | Пакеты `controller`, `service`, `repository` внутри backend-модулей | Контроллеры принимают запросы, сервисы содержат бизнес-логику, репозитории работают с БД |
| DTO и валидация входа | DTO-классы в feature-пакетах, request-классы с validation annotations | Entity не используются напрямую как API-контракт |
| Единый формат ошибок | `common/exception/GlobalExceptionHandler.kt`, `common/dto/ErrorResponse.kt` | Ошибки API возвращаются в едином формате |
| REST API | Backend controller-классы | API разделено по ролям и предметным областям |
| Swagger/OpenAPI | `springdoc-openapi`, `OpenApiConfig.kt` | Swagger UI доступен по `/swagger-ui.html` |
| PostgreSQL + JPA/Hibernate | `spring-boot-starter-data-jpa`, `postgresql`, entity/repository классы | Основное хранилище данных |
| Flyway migrations | `backend/src/main/resources/db/migration` | Схема БД поднимается автоматически |
| Нетривиальные связи данных | User-role, organization-manager, event-registration, attendance, reward-purchase | Связи отражены в ER-модели |
| Spring Security + JWT + роли | `SecurityConfig.kt`, `JwtAuthenticationFilter.kt`, `ActiveRoleFilter.kt` | Доступ ограничивается на backend и учитывается во frontend |
| Backend tests + Testcontainers | `backend/src/test/kotlin/ru/eventhub/support/PostgresTestContainer.kt` | Тесты запускаются с PostgreSQL в контейнере |
| Контейнеризация | `backend/Dockerfile`, `frontend/Dockerfile`, `docker-compose.yaml` | Compose поднимает PostgreSQL, Redis, backend и frontend |
| Redis + `@Cacheable` | `RewardService.kt`, `RewardPurchaseService.kt`, `application.yaml` | Кэшируется список активных наград |
| Async/Scheduled | `EventStatusScheduler.kt`, `EventService.completeOverduePublishedEvents()` | Плановый процесс завершает прошедшие мероприятия |
| GitHub Actions CI | `.github/workflows/backend.yml`, `.github/workflows/frontend.yml` | Backend запускает тесты, frontend запускает сборку |
| Spring Actuator | `spring-boot-starter-actuator`, `application.yaml` | Доступны health и metrics |

## Ключевые пользовательские сценарии

### Студент

1. Регистрируется и входит в систему.
2. Просматривает опубликованные мероприятия.
3. Записывается на мероприятие.
4. Просматривает свои регистрации.
5. Получает баллы после отметки посещаемости.
6. Просматривает баланс и историю баллов.
7. Покупает награду за баллы.

Frontend-страницы:

- `StudentDashboardPage.vue`
- `StudentEventsPage.vue`
- `StudentMyEventsPage.vue`
- `StudentPointsPage.vue`
- `StudentRewardsPage.vue`

### Менеджер организации

1. Просматривает свои организации.
2. Создает и редактирует мероприятия организации.
3. Публикует, отменяет и завершает мероприятия.
4. Просматривает регистрации студентов.
5. Отмечает посещаемость.

Frontend-страницы:

- `ManagerDashboardPage.vue`
- `ManagerOrganizationsPage.vue`
- `ManagerEventsPage.vue`
- `ManagerAttendancePage.vue`

### Администратор

1. Управляет пользователями.
2. Создает и редактирует организации.
3. Назначает менеджеров организаций.
4. Создает и редактирует награды.
5. Просматривает покупки наград.

Frontend-страницы:

- `AdminDashboardPage.vue`
- `AdminUsersPage.vue`
- `AdminOrganizationsPage.vue`
- `AdminRewardsPage.vue`
- `AdminPurchasesPage.vue`

## Транзакционные гарантии

| Сценарий | Риск | Защита |
| --- | --- | --- |
| Запись на мероприятие | Два студента одновременно занимают последнее место | `EventRepository.findByIdForUpdate()` блокирует мероприятие на время проверки вместимости |
| Покупка награды | Несколько студентов одновременно покупают последнюю награду | `RewardRepository.findByIdForUpdate()` блокирует награду на время проверки остатка |
| Списание баллов | Баланс может уйти в минус при одновременных покупках | `UserRepository.findByIdForUpdate()` блокирует пользователя перед расчетом баланса |

## Тесты

Тесты запускаются командой:

```bash
cd backend
./mvnw test
```

Покрытые сценарии:

- загрузка Spring context с Testcontainers PostgreSQL;
- регистрация, вход и получение текущего пользователя;
- запрет доступа студенту к admin endpoint;
- запрет записи сверх вместимости мероприятия;
- начисление баллов за посещение;
- покупка награды за баллы.

Основные тестовые файлы:

- `EventhubApplicationTests.kt`
- `AuthControllerIntegrationTest.kt`
- `EventRegistrationServiceIntegrationTest.kt`
- `RewardPurchaseServiceIntegrationTest.kt`

## Запуск и проверка

Полный запуск:

```bash
docker compose up --build
```

Проверка backend:

```bash
cd backend
./mvnw test
```

Проверка frontend:

```bash
cd frontend
npm run build
```

Проверка наблюдаемости:

```text
GET http://localhost:8080/actuator/health
GET http://localhost:8080/actuator/metrics
```

Проверка API-документации:

```text
http://localhost:8080/swagger-ui.html
```