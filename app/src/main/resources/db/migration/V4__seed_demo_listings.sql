WITH seller_main AS (
    SELECT id, city FROM users WHERE telegram_id = 999000001
),
seller_second AS (
    SELECT id, city FROM users WHERE telegram_id = 999000002
),
cat_engine AS (
    SELECT id FROM categories WHERE slug = 'engine'
),
cat_transmission AS (
    SELECT id FROM categories WHERE slug = 'transmission'
),
cat_body AS (
    SELECT id FROM categories WHERE slug = 'body'
)
INSERT INTO listings (
    seller_id,
    category_id,
    title,
    description,
    price,
    condition,
    original_replica,
    status,
    mileage_km,
    vehicle_year,
    created_at
)
SELECT seller_main.id, cat_engine.id,
       'Двигатель Kia Rio 1.6 G4FG в сборе',
       'Контрактный двигатель, пробег 74 000 км, компрессия ровная. Гарантия 14 дней на установку.',
       89000, 'used', 'original', 'active', 74000, 2018, NOW() - INTERVAL '2 days'
FROM seller_main, cat_engine
UNION ALL
SELECT seller_main.id, cat_transmission.id,
       'АКПП Hyundai Solaris A6GF1',
       'Снята с автомобиля 2019 года, без пинков и рывков. Есть видео работы.',
       67000, 'used', 'original', 'active', 91000, 2019, NOW() - INTERVAL '1 day'
FROM seller_main, cat_transmission
UNION ALL
SELECT seller_second.id, cat_body.id,
       'Крыло переднее левое Lada Vesta (белое)',
       'Оригинал, без шпаклёвки. Есть мелкая царапина по лаку.',
       8500, 'used', 'original', 'active', NULL, 2020, NOW() - INTERVAL '8 hours'
FROM seller_second, cat_body
UNION ALL
SELECT seller_second.id, cat_engine.id,
       'Стартер Toyota Camry 2.5 28100-36070',
       'Проверен на стенде, полностью рабочий. Подходит на Camry XV50/XV55.',
       12000, 'used', 'original', 'active', NULL, 2017, NOW() - INTERVAL '5 hours'
FROM seller_second, cat_engine
UNION ALL
SELECT seller_main.id, cat_body.id,
       'Бампер передний Skoda Octavia A7 рест',
       'Оригинальный бампер под парктроники и омыватель фар. Нужна покраска.',
       15000, 'used', 'original', 'active', NULL, 2019, NOW() - INTERVAL '2 hours'
FROM seller_main, cat_body;

WITH recent_listings AS (
    SELECT id, title
    FROM listings
    WHERE title IN (
        'Двигатель Kia Rio 1.6 G4FG в сборе',
        'АКПП Hyundai Solaris A6GF1',
        'Крыло переднее левое Lada Vesta (белое)',
        'Стартер Toyota Camry 2.5 28100-36070',
        'Бампер передний Skoda Octavia A7 рест'
    )
)
INSERT INTO listing_photos (listing_id, photo_url, sort_order)
SELECT id, 'https://images.unsplash.com/photo-1486006920555-c77dcf18193c?auto=format&fit=crop&w=1200&q=80', 0
FROM recent_listings WHERE title = 'Двигатель Kia Rio 1.6 G4FG в сборе'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1613214150339-3180dbd9f81f?auto=format&fit=crop&w=1200&q=80', 0
FROM recent_listings WHERE title = 'АКПП Hyundai Solaris A6GF1'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1615906655593-ad0386982a0f?auto=format&fit=crop&w=1200&q=80', 0
FROM recent_listings WHERE title = 'Крыло переднее левое Lada Vesta (белое)'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1200&q=80', 0
FROM recent_listings WHERE title = 'Стартер Toyota Camry 2.5 28100-36070'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1493238792000-8113da705763?auto=format&fit=crop&w=1200&q=80', 0
FROM recent_listings WHERE title = 'Бампер передний Skoda Octavia A7 рест';
