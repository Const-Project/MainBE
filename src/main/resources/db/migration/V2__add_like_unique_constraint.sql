WITH ranked AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
      PARTITION BY user_id, target_id, target_type
      ORDER BY created_at NULLS LAST, id
    ) AS rn
    FROM likes
)
DELETE FROM likes
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

ALTER TABLE likes
    ADD CONSTRAINT uk_likes_user_target
        UNIQUE (user_id, target_id, target_type);