-- Criação das tabelas principais: users, roles, user_roles, posts

-- roles
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO roles (name) VALUES
                             ('ROLE_USER'),
                             ('ROLE_ADMIN'),
                             ('ROLE_MODERATOR');

-- users (UUID)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       description TEXT,
                       profile_link VARCHAR(255),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       failed_login_attempts INT NOT NULL DEFAULT 0,
                       account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
                       deleted BOOLEAN DEFAULT FALSE
);

-- user_roles (join table)
CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- posts (Long ID auto increment)
CREATE TABLE posts (
                       id BIGSERIAL PRIMARY KEY,
                       user_id UUID NOT NULL,
                       title VARCHAR(255),
                       description TEXT,
                       photo_link VARCHAR(255),
                       video_link VARCHAR(255),
                       private_post BOOLEAN DEFAULT FALSE,
                       likes_count INTEGER DEFAULT 0,
                       deleted BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
