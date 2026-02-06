-- Add last_accessed_at for tracking recent user activity.
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS last_accessed_at TIMESTAMP;

-- Optional: backfill with updated_at if you want a baseline.
UPDATE users
  SET last_accessed_at = created_at
WHERE last_accessed_at IS NULL
  AND created_at >= (CURRENT_TIMESTAMP - INTERVAL '1 year');
