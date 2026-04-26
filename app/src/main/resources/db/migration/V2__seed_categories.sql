INSERT INTO categories (name, slug, parent_id, level)
VALUES ('Запчасти', 'parts', NULL, 0);

WITH root AS (SELECT id FROM categories WHERE slug = 'parts')
INSERT INTO categories (name, slug, parent_id, level)
SELECT 'Двигатель', 'engine', root.id, 1 FROM root
UNION ALL
SELECT 'КПП', 'transmission', root.id, 1 FROM root
UNION ALL
SELECT 'Кузов', 'body', root.id, 1 FROM root;

INSERT INTO category_attributes (category_id, name, slug, data_type, required, sort_order)
SELECT c.id, 'Объём', 'engine_volume', 'number', false, 1
FROM categories c WHERE c.slug = 'engine';

INSERT INTO category_attributes (category_id, name, slug, data_type, required, sort_order)
SELECT c.id, 'Тип топлива', 'fuel_type', 'string', false, 2
FROM categories c WHERE c.slug = 'engine';

INSERT INTO category_attributes (category_id, name, slug, data_type, required, sort_order)
SELECT c.id, 'Мощность', 'power_kw', 'number', false, 3
FROM categories c WHERE c.slug = 'engine';
