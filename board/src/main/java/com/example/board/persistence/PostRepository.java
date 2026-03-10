package com.example.board.persistence;

import com.example.board.domain.post.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 전체 게시물 목록 (메인 페이지용)
    Page<PostEntity> findAll(Pageable pageable);


    // 이전 게시물: ID > currentId인 가장 작은 것 (최신순)
    Optional<PostEntity> findFirstByBoardIdAndIdGreaterThanOrderByIdAsc(Long currentId);

    // 다음 게시물: ID < currentId인 가장 큰 것 (최신순)
    Optional<PostEntity> findFirstByBoardIdAndIdLessThanOrderByIdDesc(Long currentId);


}