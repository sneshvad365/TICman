CREATE TABLE requests (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID        NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    created_by    UUID        NOT NULL REFERENCES users(id),
    name          TEXT        NOT NULL,
    method        TEXT        NOT NULL DEFAULT 'GET',
    url_template  TEXT        NOT NULL DEFAULT '',
    headers       JSONB       NOT NULL DEFAULT '{}',
    body          TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
