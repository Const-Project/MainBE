ALTER TABLE users
    ADD COLUMN IF NOT EXISTS oauth_provider varchar(50),
    ADD COLUMN IF NOT EXISTS oauth_subject varchar(255);

ALTER TABLE users
    ADD CONSTRAINT users_oauth_provider_subject_uq
    UNIQUE (oauth_provider, oauth_subject);
