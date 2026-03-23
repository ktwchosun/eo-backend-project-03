package com.example.board.persistence;

import com.example.board.domain.post.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 이전 게시글
    Optional<PostEntity> findFirstByIdGreaterThanOrderByIdAsc(Long currentPostId);

    // 다음 게시글
    Optional<PostEntity> findFirstByIdLessThanOrderByIdDesc(Long currentPostId);

}