CREATE TABLE user_cars (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(255) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    generation VARCHAR(120),
    year INT,
    engine_volume DECIMAL(4,1),
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_user_cars_user ON user_cars(user_id);
CREATE UNIQUE INDEX ux_user_cars_user_active ON user_cars(user_id) WHERE active = TRUE;
