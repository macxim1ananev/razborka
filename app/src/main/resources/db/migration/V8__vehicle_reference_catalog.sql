CREATE TABLE IF NOT EXISTS vehicle_makes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS vehicle_models (
    id BIGSERIAL PRIMARY KEY,
    make_id BIGINT NOT NULL REFERENCES vehicle_makes(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    UNIQUE (make_id, name),
    UNIQUE (make_id, slug)
);

CREATE INDEX IF NOT EXISTS idx_vehicle_models_make ON vehicle_models(make_id);

CREATE TABLE IF NOT EXISTS vehicle_generations (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT NOT NULL REFERENCES vehicle_models(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    year_from INT,
    year_to INT,
    UNIQUE (model_id, name)
);

CREATE INDEX IF NOT EXISTS idx_vehicle_generations_model ON vehicle_generations(model_id);

INSERT INTO vehicle_makes (name, slug) VALUES
    ('Toyota', 'toyota'),
    ('Volkswagen', 'volkswagen'),
    ('BMW', 'bmw'),
    ('Mercedes-Benz', 'mercedes-benz'),
    ('Audi', 'audi'),
    ('Hyundai', 'hyundai'),
    ('Kia', 'kia'),
    ('LADA', 'lada')
ON CONFLICT (slug) DO NOTHING;

WITH makes AS (
    SELECT id, slug FROM vehicle_makes
)
INSERT INTO vehicle_models (make_id, name, slug)
SELECT m.id, v.name, v.slug
FROM makes m
JOIN (VALUES
    ('toyota', 'Camry', 'camry'),
    ('toyota', 'Corolla', 'corolla'),
    ('toyota', 'RAV4', 'rav4'),
    ('toyota', 'Land Cruiser', 'land-cruiser'),
    ('volkswagen', 'Passat', 'passat'),
    ('volkswagen', 'Golf', 'golf'),
    ('volkswagen', 'Tiguan', 'tiguan'),
    ('bmw', '3 Series', '3-series'),
    ('bmw', '5 Series', '5-series'),
    ('bmw', 'X5', 'x5'),
    ('mercedes-benz', 'C-Class', 'c-class'),
    ('mercedes-benz', 'E-Class', 'e-class'),
    ('mercedes-benz', 'GLC', 'glc'),
    ('audi', 'A4', 'a4'),
    ('audi', 'A6', 'a6'),
    ('audi', 'Q5', 'q5'),
    ('hyundai', 'Solaris', 'solaris'),
    ('hyundai', 'Elantra', 'elantra'),
    ('hyundai', 'Santa Fe', 'santa-fe'),
    ('kia', 'Rio', 'rio'),
    ('kia', 'Ceed', 'ceed'),
    ('kia', 'Sorento', 'sorento'),
    ('lada', 'Granta', 'granta'),
    ('lada', 'Vesta', 'vesta'),
    ('lada', 'Niva Legend', 'niva-legend')
) AS v(make_slug, name, slug)
ON m.slug = v.make_slug
ON CONFLICT (make_id, slug) DO NOTHING;

WITH models AS (
    SELECT vm.id, vm.slug, mk.slug AS make_slug
    FROM vehicle_models vm
    JOIN vehicle_makes mk ON mk.id = vm.make_id
)
INSERT INTO vehicle_generations (model_id, name, year_from, year_to)
SELECT m.id, v.name, v.year_from, v.year_to
FROM models m
JOIN (VALUES
    ('toyota', 'camry', 'XV40', 2006, 2011),
    ('toyota', 'camry', 'XV50', 2011, 2017),
    ('toyota', 'camry', 'XV70', 2017, NULL),
    ('toyota', 'corolla', 'E140/E150', 2006, 2013),
    ('toyota', 'corolla', 'E170/E180', 2013, 2019),
    ('toyota', 'corolla', 'E210', 2018, NULL),
    ('toyota', 'rav4', 'XA30', 2005, 2012),
    ('toyota', 'rav4', 'XA40', 2012, 2018),
    ('toyota', 'rav4', 'XA50', 2018, NULL),
    ('toyota', 'land-cruiser', 'J100', 1998, 2007),
    ('toyota', 'land-cruiser', 'J200', 2007, 2021),
    ('toyota', 'land-cruiser', 'J300', 2021, NULL),

    ('volkswagen', 'passat', 'B5/B5.5', 1996, 2005),
    ('volkswagen', 'passat', 'B6/B7', 2005, 2015),
    ('volkswagen', 'passat', 'B8', 2014, 2023),
    ('volkswagen', 'golf', 'Mk5', 2003, 2008),
    ('volkswagen', 'golf', 'Mk6', 2008, 2012),
    ('volkswagen', 'golf', 'Mk7', 2012, 2019),
    ('volkswagen', 'golf', 'Mk8', 2019, NULL),
    ('volkswagen', 'tiguan', 'I', 2007, 2016),
    ('volkswagen', 'tiguan', 'II', 2016, NULL),

    ('bmw', '3-series', 'E46', 1997, 2006),
    ('bmw', '3-series', 'E90', 2004, 2013),
    ('bmw', '3-series', 'F30', 2011, 2019),
    ('bmw', '3-series', 'G20', 2018, NULL),
    ('bmw', '5-series', 'E60', 2003, 2010),
    ('bmw', '5-series', 'F10', 2010, 2017),
    ('bmw', '5-series', 'G30', 2016, 2023),
    ('bmw', '5-series', 'G60', 2023, NULL),
    ('bmw', 'x5', 'E70', 2006, 2013),
    ('bmw', 'x5', 'F15', 2013, 2018),
    ('bmw', 'x5', 'G05', 2018, NULL),

    ('mercedes-benz', 'c-class', 'W204', 2007, 2014),
    ('mercedes-benz', 'c-class', 'W205', 2014, 2021),
    ('mercedes-benz', 'c-class', 'W206', 2021, NULL),
    ('mercedes-benz', 'e-class', 'W212', 2009, 2016),
    ('mercedes-benz', 'e-class', 'W213', 2016, 2023),
    ('mercedes-benz', 'e-class', 'W214', 2023, NULL),
    ('mercedes-benz', 'glc', 'X253', 2015, 2022),
    ('mercedes-benz', 'glc', 'X254', 2022, NULL),

    ('audi', 'a4', 'B8', 2007, 2015),
    ('audi', 'a4', 'B9', 2015, 2024),
    ('audi', 'a4', 'B10', 2024, NULL),
    ('audi', 'a6', 'C6', 2004, 2011),
    ('audi', 'a6', 'C7', 2011, 2018),
    ('audi', 'a6', 'C8', 2018, NULL),
    ('audi', 'q5', '8R', 2008, 2017),
    ('audi', 'q5', 'FY', 2017, NULL),

    ('hyundai', 'solaris', 'I', 2010, 2017),
    ('hyundai', 'solaris', 'II', 2017, NULL),
    ('hyundai', 'elantra', 'MD', 2010, 2015),
    ('hyundai', 'elantra', 'AD', 2015, 2020),
    ('hyundai', 'elantra', 'CN7', 2020, NULL),
    ('hyundai', 'santa-fe', 'CM', 2005, 2012),
    ('hyundai', 'santa-fe', 'DM', 2012, 2018),
    ('hyundai', 'santa-fe', 'TM', 2018, 2023),
    ('hyundai', 'santa-fe', 'MX5', 2023, NULL),

    ('kia', 'rio', 'III', 2011, 2017),
    ('kia', 'rio', 'IV', 2017, NULL),
    ('kia', 'ceed', 'ED', 2006, 2012),
    ('kia', 'ceed', 'JD', 2012, 2018),
    ('kia', 'ceed', 'CD', 2018, NULL),
    ('kia', 'sorento', 'XM', 2009, 2014),
    ('kia', 'sorento', 'UM', 2014, 2020),
    ('kia', 'sorento', 'MQ4', 2020, NULL),

    ('lada', 'granta', 'I', 2011, 2018),
    ('lada', 'granta', 'FL', 2018, NULL),
    ('lada', 'vesta', 'I', 2015, 2022),
    ('lada', 'vesta', 'NG', 2023, NULL),
    ('lada', 'niva-legend', '2121/4x4', 1977, 2021),
    ('lada', 'niva-legend', 'Niva Legend', 2021, NULL)
) AS v(make_slug, model_slug, name, year_from, year_to)
ON m.make_slug = v.make_slug AND m.slug = v.model_slug
ON CONFLICT (model_id, name) DO NOTHING;
