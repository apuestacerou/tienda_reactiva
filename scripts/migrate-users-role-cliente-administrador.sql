-- Ejecutar una vez en Neon (SQL Editor) si tu tabla users ya existía con CUSTOMER / ADMIN.
-- Después de esto, en Neon puedes hacer p. ej.:
--   UPDATE users SET role = 'ADMINISTRADOR' WHERE email = 'tu@correo.com';
-- Valores válidos: CLIENTE (por defecto al registrarse), ADMINISTRADOR.

ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_role;

UPDATE users SET role = 'CLIENTE' WHERE role IN ('CUSTOMER', 'customer');
UPDATE users SET role = 'ADMINISTRADOR' WHERE role IN ('ADMIN', 'admin');

ALTER TABLE users ALTER COLUMN role SET DEFAULT 'CLIENTE';

ALTER TABLE users ADD CONSTRAINT ck_users_role CHECK (role IN ('CLIENTE', 'ADMINISTRADOR'));
