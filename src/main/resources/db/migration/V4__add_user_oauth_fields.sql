ALTER TABLE users
    ADD COLUMN IF NOT EXISTS oauth_provider varchar(50),
    ADD COLUMN IF NOT EXISTS oauth_subject varchar(255);
