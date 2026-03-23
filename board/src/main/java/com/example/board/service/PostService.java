package com.example.board.service;

import com.example.board.domain.post.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface PostService {

    Long create(PostDto postDto, Long userId);

    PostDto read(Long postId);

    boolean update(Long postId, PostDto postDto, Long userId);

    boolean delete(Long postId, Long userId);

    Page<PostDto> getAllPosts(Pageable pageable);

    Optional<PostDto> getPreviousPost(Long currentPostId);

    Optional<PostDto> getNextPost(Long currentPostId);

    Map<String, Object> toggleLike(Long postId, Long userId);

    /**
     * 좋아요 여부 확인
     *
     * @param postId 게시글 ID
     * @param userId 로그인 사용자 ID
     * @return 좋아요 여부
     */
    boolean isLiked(Long postId, Long userId);
}