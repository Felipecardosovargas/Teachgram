-- Audit columns extra se precisar rastrear mais dados (opcional)

ALTER TABLE users
    ADD COLUMN created_by UUID,
ADD COLUMN updated_by UUID,
ADD COLUMN last_login_ip VARCHAR(45);
