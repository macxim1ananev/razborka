CREATE INDEX IF NOT EXISTS idx_listings_status_created_at_desc
    ON listings (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_listings_seller_status_created_at_desc
    ON listings (seller_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_listings_category_status_created_at_desc
    ON listings (category_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_listing_photos_listing_sort_order_id
    ON listing_photos (listing_id, sort_order, id);

CREATE INDEX IF NOT EXISTS idx_listing_compatibility_listing_brand_model
    ON listing_compatibility (listing_id, brand, model);

CREATE INDEX IF NOT EXISTS idx_favorites_created_at
    ON favorites (created_at DESC);
