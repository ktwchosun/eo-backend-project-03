package com.example.board.persistence;

import com.example.board.domain.post.PostLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
}