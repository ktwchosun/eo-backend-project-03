package com.example.board.service;

import com.example.board.domain.comment.CommentDto;
import com.example.board.domain.comment.CommentEntity;
import com.example.board.domain.post.PostEntity;
import com.example.board.domain.user.UserEntity;
import com.example.board.persistence.CommentRepository;
import com.example.board.persistence.PostRepository;
import com.example.board.persistence.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private boolean isOwner(CommentEntity commentEntity, Long userId) {
        return commentEntity.getUserId().equals(userId);
    }

    private String getNickname(Long userId) {
        return userRepository.findById(userId)
                .map(UserEntity::getNickname)
                .orElse("unknown");
    }

    private CommentDto convertToDto(CommentEntity commentEntity) {
        return CommentDto.from(commentEntity, getNickname(commentEntity.getUserId()));
    }

    @Override
    public Optional<CommentDto> create(CommentDto commentDto, Long userId) {
        log.info("COMMENT CREATE: postId={}, userId={}", commentDto.getPostId(), userId);

        if (userId == null) {
            return Optional.empty();
        }

        validateContent(commentDto.getContent());

        PostEntity postEntity = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다. id=" + commentDto.getPostId()));

        CommentEntity commentEntity = CommentEntity.builder()
                .userId(userId)
                .postEntity(postEntity)
                .content(commentDto.getContent())
                .build();

        CommentEntity savedEntity = commentRepository.save(commentEntity);
        postEntity.increaseCommentsCount();

        return Optional.of(convertToDto(savedEntity));
    }

    @Override
    public Optional<CommentDto> update(CommentDto commentDto, Long userId) {
        log.info("COMMENT UPDATE: commentId={}, userId={}", commentDto.getId(), userId);

        if (userId == null) {
            return Optional.empty();
        }

        validateContent(commentDto.getContent());

        return commentRepository.findById(commentDto.getId())
                .filter(comment -> isOwner(comment, userId))
                .map(comment -> {
                    comment.updateContent(commentDto.getContent());
                    return convertToDto(comment);
                });
    }

    @Override
    public boolean delete(Long commentId, Long userId) {
        log.info("COMMENT DELETE: commentId={}, userId={}", commentId, userId);

        if (userId == null) {
            return false;
        }

        return commentRepository.findById(commentId)
                .filter(comment -> isOwner(comment, userId))
                .map(comment -> {
                    comment.getPostEntity().decreaseCommentsCount();
                    commentRepository.delete(comment);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentDto> read(Long commentId) {
        return commentRepository.findById(commentId)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> getList(Long postId, Pageable pageable) {
        log.info("COMMENT LIST: postId={}, pageable={}", postId, pageable);

        return commentRepository.findByPostEntityIdOrderByIdDesc(postId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }

        if (content.length() > 200) {
            throw new IllegalArgumentException("최대 200자까지 입력 가능합니다.");
        }
    }
}