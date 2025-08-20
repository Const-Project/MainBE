ALTER TABLE likes
    ADD CONSTRAINT uk_likes_user_target
        UNIQUE (user_id, target_id, target_type);