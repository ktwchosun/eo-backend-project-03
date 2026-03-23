package com.example.board.service;

import com.example.board.domain.post.PostDto;
import com.example.board.domain.post.PostEntity;
import com.example.board.domain.post.PostLikeEntity;
import com.example.board.domain.user.UserEntity;
import com.example.board.persistence.PostLikeRepository;
import com.example.board.persistence.PostRepository;
import com.example.board.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    /**
     * 게시글 작성
     */
    @Override
    public Long create(PostDto postDto, Long userId) {
        log.info("CREATE: postDto={}, userId={}", postDto, userId);

        validatePost(postDto);

        PostEntity postEntity = PostEntity.builder()
                .userId(userId)
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .build();

        PostEntity savedEntity = postRepository.save(postEntity);
        log.info("CREATE SUCCESS: postId={}", savedEntity.getId());

        return savedEntity.getId();
    }

    /**
     * 게시글 상세 조회 (조회수 증가 포함)
     */
    @Override
    public PostDto read(Long postId) {
        log.info("READ: postId={}", postId);

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));

        postEntity.increaseViewCount();

        return convertToDto(postEntity);
    }

    /**
     * 게시글 수정
     */
    @Override
    public boolean update(Long postId, PostDto postDto, Long userId) {
        log.info("UPDATE: postId={}, userId={}", postId, userId);

        validatePost(postDto);

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));

        if (!postEntity.getUserId().equals(userId)) {
            log.warn("UPDATE DENIED: postId={}, requestUserId={}, ownerUserId={}",
                    postId, userId, postEntity.getUserId());
            return false;
        }

        postEntity.updatePost(postDto.getTitle(), postDto.getContent());
        log.info("UPDATE SUCCESS: postId={}", postId);
        return true;
    }

    /**
     * 게시글 삭제
     */
    @Override
    public boolean delete(Long postId, Long userId) {
        log.info("DELETE: postId={}, userId={}", postId, userId);

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));

        if (!postEntity.getUserId().equals(userId)) {
            log.warn("DELETE DENIED: postId={}, requestUserId={}, ownerUserId={}",
                    postId, userId, postEntity.getUserId());
            return false;
        }

        postRepository.delete(postEntity);
        log.info("DELETE SUCCESS: postId={}", postId);
        return true;
    }

    /**
     * 전체 게시글 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(Pageable pageable) {
        log.info("GET ALL POSTS: pageable={}", pageable);

        return postRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * 이전 게시글 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PostDto> getPreviousPost(Long currentPostId) {
        log.info("GET PREVIOUS POST: currentPostId={}", currentPostId);

        return postRepository.findFirstByIdGreaterThanOrderByIdAsc(currentPostId)
                .map(this::convertToDto);
    }

    /**
     * 다음 게시글 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PostDto> getNextPost(Long currentPostId) {
        log.info("GET NEXT POST: currentPostId={}", currentPostId);

        return postRepository.findFirstByIdLessThanOrderByIdDesc(currentPostId)
                .map(this::convertToDto);
    }

    /**
     * 좋아요 토글
     */
    @Override
    public Map<String, Object> toggleLike(Long postId, Long userId) {
        log.info("TOGGLE LIKE: postId={}, userId={}", postId, userId);

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + postId));

        Map<String, Object> result = new HashMap<>();

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            postLikeRepository.deleteByPostIdAndUserId(postId, userId);
            postEntity.decreaseLikesCount();
            result.put("liked", false);
        } else {
            PostLikeEntity postLikeEntity = PostLikeEntity.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();

            postLikeRepository.save(postLikeEntity);
            postEntity.increaseLikesCount();
            result.put("liked", true);
        }

        result.put("likeCount", postEntity.getLikesCount());
        return result;
    }

    /**
     * 좋아요 여부 확인
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isLiked(Long postId, Long userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    /**
     * PostEntity -> PostDto 변환
     */
    @Transactional(readOnly = true)
    protected PostDto convertToDto(PostEntity postEntity) {
        String writerNickname = userRepository.findById(postEntity.getUserId())
                .map(UserEntity::getNickname)
                .orElse("unknown");

        return PostDto.from(postEntity, writerNickname);
    }

    /**
     * 게시글 제목/내용 검증
     */
    private void validatePost(PostDto postDto) {
        if (postDto == null) {
            throw new IllegalArgumentException("게시글 정보가 없습니다.");
        }

        if (postDto.getTitle() == null || postDto.getTitle().isBlank()) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        if (postDto.getTitle().length() > 100) {
            throw new IllegalArgumentException("제목은 100자 이하로 입력해주세요.");
        }

        if (postDto.getContent() == null || postDto.getContent().isBlank()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
    }
}