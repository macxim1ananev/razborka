-- Расширенная рубрикаторика: крупные разделы и подкатегории (ориентир — универсальный классифайд).
-- Слаги уникальны глобально (ограничение categories.slug).

INSERT INTO categories (name, slug, parent_id, level) VALUES
    ('Транспорт', 'transport', NULL, 0),
    ('Недвижимость', 'realty', NULL, 0),
    ('Работа', 'jobs', NULL, 0),
    ('Услуги', 'services', NULL, 0),
    ('Личные вещи', 'personal', NULL, 0),
    ('Для дома и дачи', 'home_garden', NULL, 0),
    ('Бытовая электроника', 'electronics', NULL, 0),
    ('Хобби и отдых', 'hobby', NULL, 0),
    ('Животные', 'animals', NULL, 0),
    ('Для бизнеса', 'business', NULL, 0),
    ('Детские товары', 'kids', NULL, 0),
    ('Отдам даром', 'free_stuff', NULL, 0),
    ('Одежда, обувь, аксессуары', 'fashion', NULL, 0),
    ('Красота и здоровье', 'beauty_health', NULL, 0),
    ('Ремонт и строительство', 'renovation', NULL, 0),
    ('Спорт и отдых', 'sports_outdoor', NULL, 0),
    ('Охота и рыбалка', 'hunting_fishing', NULL, 0),
    ('Сад и огород', 'garden_plot', NULL, 0),
    ('Книги и учебники', 'books_media', NULL, 0),
    ('Коллекционирование', 'collectibles', NULL, 0),
    ('Музыкальные инструменты', 'music_gear', NULL, 0),
    ('Настольные игры и модели', 'boardgames_models', NULL, 0),
    ('Билеты и путешествия', 'tickets_travel', NULL, 0),
    ('Всё для свадьбы', 'wedding', NULL, 0),
    ('Велосипеды', 'bicycles', NULL, 0),
    ('Водный спорт', 'water_sports', NULL, 0),
    ('Конный спорт', 'equestrian', NULL, 0);

-- Транспорт
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Легковые автомобили', 'transport_cars'),
                         ('Мотоциклы и мототехника', 'transport_moto'),
                         ('Грузовики и спецтехника', 'transport_trucks'),
                         ('Водный транспорт', 'transport_water'),
                         ('Воздушный транспорт', 'transport_air'),
                         ('Прицепы', 'transport_trailers'),
                         ('Снегоходы и вездеходы', 'transport_atv'),
                         ('Автодома и кемперы', 'transport_rv'),
                         ('Шины, диски и колёса', 'transport_tires'),
                         ('Масла и автохимия', 'transport_fluids'),
                         ('Аудио- и видеотехника в авто', 'transport_car_av'),
                         ('Аксессуары для авто', 'transport_car_accessories'),
                         ('Багажники и фаркопы', 'transport_racks'),
                         ('Уход, химчистка, тонировка', 'transport_care')) AS v(name, slug)
WHERE t.slug = 'transport';

-- Недвижимость
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Квартиры', 'realty_flats'),
                         ('Комнаты', 'realty_rooms'),
                         ('Дома, дачи, коттеджи', 'realty_houses'),
                         ('Земельные участки', 'realty_land'),
                         ('Коммерческая недвижимость', 'realty_commercial'),
                         ('Гаражи и машиноместа', 'realty_garage'),
                         ('Аренда квартир', 'realty_rent_flats'),
                         ('Аренда комнат', 'realty_rent_rooms'),
                         ('Посуточная аренда', 'realty_daily_rent'),
                         ('Зарубежная недвижимость', 'realty_abroad')) AS v(name, slug)
WHERE t.slug = 'realty';

-- Работа
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Вакансии', 'jobs_vacancies'),
                         ('Резюме', 'jobs_resume'),
                         ('Подработка', 'jobs_parttime'),
                         ('Стажировки', 'jobs_internship')) AS v(name, slug)
WHERE t.slug = 'jobs';

-- Услуги
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Перевозки и логистика', 'services_shipping'),
                         ('Автосервис', 'services_auto'),
                         ('Ремонт и отделка', 'services_renovation'),
                         ('Клининг и уборка', 'services_cleaning'),
                         ('Компьютерная помощь', 'services_it'),
                         ('Обучение и курсы', 'services_education'),
                         ('Красота и здоровье', 'services_beauty'),
                         ('Организация мероприятий', 'services_events'),
                         ('Юридические и бухгалтерские', 'services_legal'),
                         ('Фото- и видеосъёмка', 'services_photo'),
                         ('Ремонт техники', 'services_gadget_repair'),
                         ('Мастер на час', 'services_handyman'),
                         ('Охрана и детективы', 'services_security'),
                         ('Дизайн и полиграфия', 'services_design'),
                         ('Реклама и маркетинг', 'services_marketing')) AS v(name, slug)
WHERE t.slug = 'services';

-- Личные вещи
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Одежда', 'personal_clothes'),
                         ('Обувь', 'personal_shoes'),
                         ('Часы и украшения', 'personal_jewelry'),
                         ('Сумки и рюкзаки', 'personal_bags'),
                         ('Аксессуары', 'personal_accessories'),
                         ('Косметика и парфюмерия', 'personal_cosmetics'),
                         ('Подарки и сувениры', 'personal_gifts')) AS v(name, slug)
WHERE t.slug = 'personal';

-- Для дома и дачи
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Мебель и интерьер', 'home_furniture'),
                         ('Кухня и столовая', 'home_kitchen'),
                         ('Текстиль и ковры', 'home_textile'),
                         ('Освещение', 'home_lighting'),
                         ('Хранение вещей', 'home_storage'),
                         ('Бытовая техника', 'home_appliances'),
                         ('Растения и рассада', 'home_plants'),
                         ('Посуда и кухонные принадлежности', 'home_cookware'),
                         ('Декор', 'home_decor')) AS v(name, slug)
WHERE t.slug = 'home_garden';

-- Бытовая электроника
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Телефоны', 'elec_phones'),
                         ('Планшеты', 'elec_tablets'),
                         ('Ноутбуки', 'elec_laptops'),
                         ('Настольные ПК', 'elec_desktops'),
                         ('Мониторы', 'elec_monitors'),
                         ('Комплектующие', 'elec_components'),
                         ('Периферия', 'elec_peripherals'),
                         ('Сетевое оборудование', 'elec_network'),
                         ('Фото- и видеокамеры', 'elec_photo'),
                         ('Наушники и аудио', 'elec_audio'),
                         ('Игровые приставки', 'elec_consoles'),
                         ('Игры и софт', 'elec_games'),
                         ('Умный дом и гаджеты', 'elec_smart'),
                         ('Телевизоры', 'elec_tv'),
                         ('Оргтехника', 'elec_office')) AS v(name, slug)
WHERE t.slug = 'electronics';

-- Хобби и отдых
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Спортивный инвентарь', 'hobby_sports_gear'),
                         ('Туризм и кемпинг', 'hobby_camping'),
                         ('Ролики и скейтборды', 'hobby_skates'),
                         ('Йога и фитнес', 'hobby_fitness'),
                         ('Настольные игры', 'hobby_boardgames'),
                         ('Рукоделие', 'hobby_crafts'),
                         ('Радиоуправляемые модели', 'hobby_rc'),
                         ('Антиквариат', 'hobby_antiques')) AS v(name, slug)
WHERE t.slug = 'hobby';

-- Животные
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Собаки', 'anim_dogs'),
                         ('Кошки', 'anim_cats'),
                         ('Птицы', 'anim_birds'),
                         ('Аквариум', 'anim_aquarium'),
                         ('Грызуны', 'anim_rodents'),
                         ('Рептилии', 'anim_reptiles'),
                         ('Сельхозживотные', 'anim_farm'),
                         ('Товары для животных', 'anim_supplies'),
                         ('Вязка', 'anim_breeding'),
                         ('Бюро находок', 'anim_lost')) AS v(name, slug)
WHERE t.slug = 'animals';

-- Для бизнеса
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Оборудование для бизнеса', 'bus_equipment'),
                         ('Торговое оборудование', 'bus_retail'),
                         ('Ресторан и кафе', 'bus_horeca'),
                         ('Салоны красоты', 'bus_beauty_salon'),
                         ('Медицина и фармацевтика', 'bus_medical'),
                         ('Производство', 'bus_manufacturing'),
                         ('Сельхозтехника', 'bus_agro'),
                         ('IT-оборудование', 'bus_it_hw')) AS v(name, slug)
WHERE t.slug = 'business';

-- Детские товары
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Детская одежда', 'kids_clothes'),
                         ('Детская обувь', 'kids_shoes'),
                         ('Игрушки', 'kids_toys'),
                         ('Коляски и автокресла', 'kids_strollers'),
                         ('Детская мебель', 'kids_furniture'),
                         ('Кормление и уход', 'kids_care'),
                         ('Школа и канцтовары', 'kids_school')) AS v(name, slug)
WHERE t.slug = 'kids';

-- Отдам даром
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Отдам даром', 'free_giveaway'),
                         ('Обмен', 'free_barter'),
                         ('Возьму даром', 'free_wanted')) AS v(name, slug)
WHERE t.slug = 'free_stuff';

-- Мода (корень)
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Женская одежда', 'fashion_women'),
                         ('Мужская одежда', 'fashion_men'),
                         ('Детская мода', 'fashion_kids'),
                         ('Обувь', 'fashion_shoes'),
                         ('Свадебные платья', 'fashion_wedding'),
                         ('Часы', 'fashion_watches'),
                         ('Украшения', 'fashion_jewelry')) AS v(name, slug)
WHERE t.slug = 'fashion';

-- Красота и здоровье (корень)
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Косметика', 'beauty_cosmetics'),
                         ('Парфюмерия', 'beauty_perfume'),
                         ('Уход за волосами', 'beauty_hair'),
                         ('Маникюр и педикюр', 'beauty_nails'),
                         ('Оптика', 'beauty_optics'),
                         ('Массажёры и тренажёры', 'beauty_massage'),
                         ('Медтовары', 'beauty_medical')) AS v(name, slug)
WHERE t.slug = 'beauty_health';

-- Ремонт и строительство
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Инструменты', 'reno_tools'),
                         ('Сантехника', 'reno_plumbing'),
                         ('Электрика', 'reno_electric'),
                         ('Отделочные материалы', 'reno_finishing'),
                         ('Окна и двери', 'reno_windows'),
                         ('Потолки и полы', 'reno_floors'),
                         ('Строительные материалы', 'reno_build'),
                         ('Садовая техника', 'reno_garden_tools')) AS v(name, slug)
WHERE t.slug = 'renovation';

-- Спорт и отдых (корень)
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Зимний спорт', 'sports_winter'),
                         ('Летний спорт', 'sports_summer'),
                         ('Командные виды', 'sports_team'),
                         ('Единоборства', 'sports_martial'),
                         ('Тренажёры', 'sports_gym'),
                         ('Велосипеды и запчасти', 'sports_bike_parts'),
                         ('Ролики и скейты', 'sports_skate'),
                         ('Туризм', 'sports_tourism')) AS v(name, slug)
WHERE t.slug = 'sports_outdoor';

-- Охота и рыбалка
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Рыболовные снасти', 'hf_fishing'),
                         ('Охотничье снаряжение', 'hf_hunting'),
                         ('Ножи и мультитулы', 'hf_knives'),
                         ('Одежда для охоты и рыбалки', 'hf_clothing')) AS v(name, slug)
WHERE t.slug = 'hunting_fishing';

-- Сад и огород
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Семена и рассада', 'garden_seeds'),
                         ('Удобрения', 'garden_fertilizer'),
                         ('Теплицы и парники', 'garden_greenhouse'),
                         ('Инвентарь', 'garden_tools'),
                         ('Мебель для сада', 'garden_furniture'),
                         ('Бассейны и бани', 'garden_pools')) AS v(name, slug)
WHERE t.slug = 'garden_plot';

-- Книги и медиа
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Художественная литература', 'books_fiction'),
                         ('Учебники и пособия', 'books_textbooks'),
                         ('Комиксы и журналы', 'books_comics'),
                         ('Аудио- и электронные книги', 'books_digital'),
                         ('Канцтовары', 'books_stationery')) AS v(name, slug)
WHERE t.slug = 'books_media';

-- Коллекционирование
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Марки и монеты', 'coll_stamps'),
                         ('Открытки', 'coll_postcards'),
                         ('Модели', 'coll_models'),
                         ('Винил и CD', 'coll_music_media'),
                         ('Искусство', 'coll_art')) AS v(name, slug)
WHERE t.slug = 'collectibles';

-- Музыкальные инструменты
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Гитары', 'music_guitars'),
                         ('Клавишные', 'music_keys'),
                         ('Ударные', 'music_drums'),
                         ('Духовые и смычковые', 'music_wind_strings'),
                         ('Студийное оборудование', 'music_studio'),
                         ('Аксессуары', 'music_accessories')) AS v(name, slug)
WHERE t.slug = 'music_gear';

-- Настольные игры и модели
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Настольные игры', 'bgm_boardgames'),
                         ('Warhammer и варгейм', 'bgm_wargame'),
                         ('Пазлы', 'bgm_puzzles'),
                         ('Сборные модели', 'bgm_models'),
                         ('LEGO и конструкторы', 'bgm_lego')) AS v(name, slug)
WHERE t.slug = 'boardgames_models';

-- Билеты и путешествия
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Авиабилеты', 'tt_flights'),
                         ('Ж/д и автобусы', 'tt_ground'),
                         ('Концерты и мероприятия', 'tt_events'),
                         ('Туры и путёвки', 'tt_tours'),
                         ('Сертификаты', 'tt_vouchers')) AS v(name, slug)
WHERE t.slug = 'tickets_travel';

-- Свадьба
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Платья и костюмы', 'wed_dresses'),
                         ('Украшения', 'wed_jewelry'),
                         ('Декор и флористика', 'wed_decor'),
                         ('Фото и видео', 'wed_media'),
                         ('Авто на свадьбу', 'wed_cars')) AS v(name, slug)
WHERE t.slug = 'wedding';

-- Велосипеды
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Горные', 'bike_mtb'),
                         ('Шоссейные', 'bike_road'),
                         ('Городские', 'bike_city'),
                         ('BMX', 'bike_bmx'),
                         ('Детские', 'bike_kids'),
                         ('Запчасти и аксессуары', 'bike_parts')) AS v(name, slug)
WHERE t.slug = 'bicycles';

-- Водный спорт
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Доски и серф', 'ws_surf'),
                         ('SUP и каяки', 'ws_sup'),
                         ('Гидрокостюмы', 'ws_wetsuits'),
                         ('Лодки и моторы', 'ws_boats')) AS v(name, slug)
WHERE t.slug = 'water_sports';

-- Конный спорт
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, t.id, 1
FROM categories t
         CROSS JOIN (VALUES
                         ('Седла и амуниция', 'eq_saddles'),
                         ('Одежда для всадника', 'eq_rider'),
                         ('Корм и добавки', 'eq_feed'),
                         ('Ветаптека', 'eq_vet')) AS v(name, slug)
WHERE t.slug = 'equestrian';
