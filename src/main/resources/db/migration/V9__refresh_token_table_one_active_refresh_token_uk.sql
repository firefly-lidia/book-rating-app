CREATE UNIQUE INDEX uq_refresh_one_active
  ON refresh_token(user_id)
  WHERE revoked = FALSE;