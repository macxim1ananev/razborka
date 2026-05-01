CREATE TABLE chat_threads (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    buyer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seller_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    last_message_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_chat_threads_listing_buyer UNIQUE (listing_id, buyer_id)
);

CREATE INDEX idx_chat_threads_buyer ON chat_threads(buyer_id);
CREATE INDEX idx_chat_threads_seller ON chat_threads(seller_id);
CREATE INDEX idx_chat_threads_listing ON chat_threads(listing_id);
CREATE INDEX idx_chat_threads_activity ON chat_threads(last_message_at DESC, created_at DESC);

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL REFERENCES chat_threads(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    read_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_chat_messages_thread_created ON chat_messages(thread_id, created_at DESC, id DESC);
CREATE INDEX idx_chat_messages_unread ON chat_messages(thread_id, sender_id, read_at);
