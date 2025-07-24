-- Populando a tabela de roles com valores iniciais

INSERT INTO roles (name) VALUES
                             ('ROLE_USER'),
                             ('ROLE_ADMIN'),
                             ('ROLE_MODERATOR')
    ON CONFLICT (name) DO NOTHING;
