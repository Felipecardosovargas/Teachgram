package com.felipe.teachgram_backend.repository;

import com.felipe.teachgram_backend.entity.Follow;
import com.felipe.teachgram_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerAndFollowing(User follower, User following);

    List<Follow> findAllByFollower(User follower);
    List<Follow> findAllByFollowing(User following);

    void deleteByFollowerAndFollowing(User follower, User following);
}
