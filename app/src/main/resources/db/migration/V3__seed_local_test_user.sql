-- Локальные тестовые учётные записи (telegram_id в диапазоне 999xxxxxxx не пересекается с реальными ID).
-- Для выдачи JWT используйте профиль Spring dev и POST /auth/dev/token (см. README).

INSERT INTO users (telegram_id, username, first_name, phone, city, bio, status, created_at)
VALUES
    (999000001, 'local_tester', 'Локальный тест', '+79000000001', 'Москва',
     'Тестовый пользователь для локального запуска API (покупатель/продавец).', 'ACTIVE', NOW()),
    (999000002, 'local_seller', 'Второй тест', '+79000000002', 'Санкт-Петербург',
     'Второй тестовый пользователь (например, для сценариев между двумя аккаунтами).', 'ACTIVE', NOW())
ON CONFLICT (telegram_id) DO NOTHING;
