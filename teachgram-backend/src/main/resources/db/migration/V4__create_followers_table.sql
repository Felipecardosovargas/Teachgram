-- followers com id UUID (como na entidade Follow.java)

CREATE TABLE follows (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         follower_id UUID NOT NULL,
                         following_id UUID NOT NULL,
                         followed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT fk_following FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT chk_self_follow CHECK (follower_id <> following_id),
                         CONSTRAINT uc_follow_pair UNIQUE (follower_id, following_id)
);
