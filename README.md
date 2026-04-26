# Маркетплейс автозапчастей (Spring Boot 3)

Монолит с модулями Maven: **auth**, **user**, **listing**, **search**, **common**, исполняемый модуль **app**.

## Требования

- JDK 17+
- Maven 3.9+
- Docker и Docker Compose (для PostgreSQL, Redis, Elasticsearch)

## Запуск инфраструктуры

```bash
docker compose up -d
```

Скопируйте `.env.example` в `.env` и задайте `TELEGRAM_BOT_TOKEN` и при необходимости `JWT_SECRET`.

## Запуск приложения

```bash
mvn -pl app spring-boot:run
```

### Локальные тестовые пользователи и JWT без Telegram

После применения миграций Flyway **V3** в БД создаются два пользователя (фиктивные `telegram_id` в диапазоне `999xxxxxxx`):

| telegram_id | username     | Назначение        |
|------------|--------------|-------------------|
| `999000001` | `local_tester` | основной тестовый аккаунт |
| `999000002` | `local_seller` | второй аккаунт (два пользователя) |

С профилем **`dev`** доступен эндпоинт выдачи пары JWT **без виджета Telegram** (в продакшене бин не регистрируется):

```bash
mvn -pl app spring-boot:run -Dspring-boot.run.profiles=dev
```

```http
POST http://localhost:8080/auth/dev/token
```

По умолчанию выдаётся токен для `telegramId=999000001`. Другой тестовый пользователь:

```http
POST http://localhost:8080/auth/dev/token?telegramId=999000002
```

**Важно:** не включайте профиль `dev` на публичных стендах и в продакшене.

Или после сборки:

```bash
mvn -DskipTests package
java -jar app/target/app-1.0.0-SNAPSHOT.jar
```

Профиль `docker` (подключение к сервисам по именам из Compose):

```bash
java -jar app/target/app-1.0.0-SNAPSHOT.jar --spring.profiles.active=docker
```

При запуске с хоста оставьте профиль по умолчанию: в `application.yml` указаны `localhost` для БД, Redis и Elasticsearch.

## Клиент (Next.js)

В каталоге [web/](web/):

```bash
cd web
cp .env.local.example .env.local
npm install
npm run dev
```

(В Windows: `copy .env.local.example .env.local`.)

Откройте `http://localhost:3000`. В `.env.local` укажите `NEXT_PUBLIC_API_URL=http://localhost:8080`, имя бота `NEXT_PUBLIC_TELEGRAM_BOT_NAME` и при необходимости `NEXT_PUBLIC_DEV_LOGIN=true` для кнопок dev-входа.

Бэкенд должен разрешать CORS с `http://localhost:3000` (по умолчанию в `application.yml`: `app.cors.allowed-origins`).

## API и документация

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Примеры запросов: [docs/api-examples.http](docs/api-examples.http).

## Аутентификация

1. Создайте бота в [@BotFather](https://t.me/BotFather), получите токен.
2. На фронте используйте [Telegram Login Widget](https://core.telegram.org/widgets/login); передайте `initData` на `POST /auth/telegram`.
3. Подпись проверяется по алгоритму виджета (секретный ключ — SHA-256 от токена бота).
4. Ответ содержит `accessToken` и `refreshToken` (15 мин / 30 дней); при включённых cookie (`app.jwt.cookie.enabled`) токены дублируются в HttpOnly cookie.

## Кэш

Первые страницы ленты (`page` 0–4) кэшируются через **Caffeine** (`listingPreviews`). Redis поднят в Compose для дальнейшего использования (сессии, rate limit и т.д.).

## Интеграционный тест с Testcontainers

С Docker:

```bash
set RUN_DOCKER_IT=true
mvn -pl app test -Dtest=MarketplaceApplicationIT
```

Без переменной тест пропускается.

## Сборка образа приложения

Сначала `mvn -DskipTests package`, затем из корня репозитория:

```bash
docker build -t marketplace-app .
```

Образ ожидает JAR по пути `app/target/app-1.0.0-SNAPSHOT.jar`.
