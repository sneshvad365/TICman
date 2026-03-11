CREATE TABLE response_history (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id   UUID        NOT NULL REFERENCES requests(id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users(id),
    status_code  INT         NOT NULL,
    headers      JSONB       NOT NULL DEFAULT '{}',
    body         TEXT,
    duration_ms  BIGINT      NOT NULL,
    executed_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
