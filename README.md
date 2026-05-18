# EventHub

EventHub, или "ИвентХаб", - веб-приложение для университетской экосистемы мероприятий.

Организации публикуют мероприятия, студенты записываются и посещают их, менеджеры организаций отмечают посещаемость, а система начисляет студентам баллы. Баллы можно обменивать на мерч и награды. Главный администратор управляет пользователями, организациями, менеджерами и наградами.

## Стек

Backend: Kotlin, Spring Boot, Maven, Spring Security, Spring Data JPA, PostgreSQL, Flyway, JWT, REST API, springdoc OpenAPI, Redis, Actuator, Testcontainers.

Frontend: Vue 3, TypeScript, Vite, Vue Router, Pinia, Tailwind CSS.

Infrastructure: Docker Compose, GitHub Actions.

## Роли

- `STUDENT` - студент.
- `ORG_MANAGER` - менеджер организации.
- `ADMIN` - главный администратор.

Один пользователь может иметь несколько ролей. После входа пользователь выбирает активную роль, если ролей больше одной. Frontend передает ее в `X-Active-Role`, backend обязан проверить, что эта роль действительно есть у пользователя.

## Модули backend

- `auth` - регистрация, вход, JWT, текущий пользователь.
- `user` - пользователи, роли, связи пользователь-роль.
- `organization` - организации и менеджеры.
- `event` - мероприятия и регистрации.
- `attendance` - посещаемость.
- `points` - баллы.
- `reward` - награды и покупки.
- `admin` - административные контроллеры.
- `common` - ошибки и общие DTO.
- `config` - Security и OpenAPI.

## Документация

- `docs/ER.md` - ER-модель.
- `docs/UseCase.md` - сценарии использования.
- `docs/coursework-compliance.md` - соответствие требованиям курсовой работы.

## Локальный запуск

### Через Docker Compose

Полный стек приложения поднимается одной командой:

```bash
docker compose up --build
```

После запуска доступны:

- frontend: http://localhost:3000
- backend API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator health: http://localhost:8080/actuator/health
- Actuator metrics: http://localhost:8080/actuator/metrics

Переменные окружения для Docker Compose находятся в `.env`. Пример заполнения хранится в `.env.example`.

### Локальный запуск backend

Для запуска backend из IDE или терминала нужны PostgreSQL и Redis:

```bash
docker compose up -d postgres redis
```

Backend использует переменные из `backend/.env`.

Команда запуска тестов:

```bash
cd backend
./mvnw test
```

Интеграционные тесты используют Testcontainers и не требуют локальной PostgreSQL.

### Локальный запуск frontend

```bash
cd frontend
npm install
npm run dev
```

Команда production-сборки:

```bash
cd frontend
npm run build
```

Frontend использует `frontend/.env`:

```env
VITE_API_BASE_URL=/api
```

## Инфраструктура

- PostgreSQL используется как основная база данных.
- Flyway автоматически применяет миграции при запуске backend.
- Redis используется для кэширования часто читаемых данных.
- Spring Actuator открывает health/metrics endpoint'ы.
- Docker Compose поднимает PostgreSQL, Redis, backend и frontend.
- GitHub Actions проверяет backend-тесты и frontend-сборку.

## Ключевые backend-сценарии

- Регистрация и вход пользователя через JWT.
- Выбор активной роли через заголовок `X-Active-Role`.
- Управление организациями, менеджерами, мероприятиями и наградами.
- Запись студента на мероприятие с защитой от превышения вместимости.
- Отметка посещаемости и начисление баллов.
- Покупка награды за баллы с защитой от отрицательного баланса и превышения остатка.
- Кэширование списка активных наград через Redis.
- Автоматическое завершение прошедших опубликованных мероприятий через `@Scheduled`.

## Проверка требований курсовой

Подробная таблица соответствия требованиям находится в:

- `docs/coursework-compliance.md`

## CI

В проекте настроены два workflow:

- `.github/workflows/backend.yml` запускает `./mvnw test`.
- `.github/workflows/frontend.yml` запускает `npm ci` и `npm run build`.