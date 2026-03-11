CREATE TABLE workspaces (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT        NOT NULL,
    owner_id    UUID        NOT NULL REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE workspace_members (
    workspace_id  UUID  NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id       UUID  NOT NULL REFERENCES users(id),
    role          TEXT  NOT NULL CHECK (role IN ('owner', 'editor', 'viewer')),
    PRIMARY KEY (workspace_id, user_id)
);
