package com.example.board.service;

import com.example.board.domain.comment.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CommentService {

    Optional<CommentDto> create(CommentDto commentDto, Long userId);

    Optional<CommentDto> update(CommentDto commentDto, Long userId);

    boolean delete(Long commentId, Long userId);

    Optional<CommentDto> read(Long commentId);

    Page<CommentDto> getList(Long postId, Pageable pageable);

    Page<CommentDto> getAllComments(Pageable pageable);
}