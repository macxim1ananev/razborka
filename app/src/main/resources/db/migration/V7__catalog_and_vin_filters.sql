ALTER TABLE listings
    ADD COLUMN IF NOT EXISTS vin VARCHAR(17),
    ADD COLUMN IF NOT EXISTS catalog_block VARCHAR(32);

ALTER TABLE listing_compatibility
    ADD COLUMN IF NOT EXISTS generation VARCHAR(120);

CREATE INDEX IF NOT EXISTS idx_listings_vin ON listings (vin);
CREATE INDEX IF NOT EXISTS idx_listings_catalog_block ON listings (catalog_block);
CREATE INDEX IF NOT EXISTS idx_listing_compatibility_generation ON listing_compatibility (generation);

WITH parts_root AS (
    SELECT id FROM categories WHERE slug = 'parts'
)
INSERT INTO categories (name, slug, parent_id, level)
SELECT v.name, v.slug, parts_root.id, 1
FROM parts_root
CROSS JOIN (VALUES
    ('Ходовая', 'parts_suspension'),
    ('Рулевое управление', 'parts_steering'),
    ('Тормозная система', 'parts_brakes'),
    ('Электрика', 'parts_electrical'),
    ('Охлаждение', 'parts_cooling'),
    ('Топливная система', 'parts_fuel'),
    ('Выхлопная система', 'parts_exhaust'),
    ('Салон', 'parts_interior')
) AS v(name, slug)
WHERE NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = v.slug);
