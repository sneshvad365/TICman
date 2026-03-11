-- Remove all auth/user-specific columns and tables
DROP TABLE IF EXISTS workspace_members;

ALTER TABLE response_history DROP COLUMN IF EXISTS user_id;
ALTER TABLE requests         DROP COLUMN IF EXISTS created_by;
ALTER TABLE workspaces       DROP COLUMN IF EXISTS owner_id;

DROP TABLE IF EXISTS users;
