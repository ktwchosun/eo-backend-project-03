package com.example.board.service;

import com.example.board.domain.post.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.Map;
import java.util.Optional;

public interface PostService {
    /**
     * 게시글 작성
     *
     * @param boardId 게시판 ID
     * @param postDto 게시글 정보
     * @param userId 로그인 사용자 ID
     * @return 생성된 게시글 ID
     */
    Long create (Long boardId, PostDto postDto, Long userId);

    /**
     * 게시글 조회 (조회수 증가 포함)
     * @param id 게시글 ID
     * @return 조회한 게시글 정보
     */
    PostDto read(Long id);

    /**
     * 게시글 수정 (작성자만 가능)
     * @param postDto 수정할 게시글 정보
     * @param userId 로그인 사용자 ID
     * @return 수정 성공 여부
     */
    boolean update(PostDto postDto, Long userId);

    /**
     * 게시글 삭제 (작성자, 관리자 가능)
     * @param id 게시글 ID
     * @param userId 로그인 사용자 ID
     * @return 삭제 성공 여부
     */
    boolean delete(Long id, Long userId);

    /**
     * 전체 게시글 목록 조회
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<PostDto> getAllPosts(Pageable pageable);


    /**
     * 이전 게시물 조회 (게시판별, 최신순)
     * @param currentPostId 현재 게시글 ID
     * @return 이전 게시글 (없으면 empty)
     */
    Optional<PostDto> getPreviousPost(Long currentPostId);

    /**
     * 다음 게시물 조회 (게시판별, 최신순)
     * @param currentPostId 현재 게시글 ID
     * @return 다음 게시글 (없으면 empty)
     */
    Optional<PostDto> getNextPost(Long currentPostId);

    // 좋아요 토글 (좋아요/취소)
    Map<String, Object> toggleLike(Long postId, Long userId);

    // 좋아요 여부 확인
    boolean isLiked(Long postId, Long userId);
}