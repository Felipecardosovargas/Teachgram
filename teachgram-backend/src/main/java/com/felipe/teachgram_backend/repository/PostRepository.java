package com.felipe.teachgram_backend.repository;

import com.felipe.teachgram_backend.entity.Post;
import com.felipe.teachgram_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUser(User user);

    Page<Post> findByUser(User user, Pageable pageable);

    Page<Post> findByUserAndPrivatePostFalse(User user, Pageable pageable);

    Page<Post> findByPrivatePostFalse(Pageable pageable);

}