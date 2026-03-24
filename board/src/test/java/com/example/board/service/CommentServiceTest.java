package com.example.board.service;

import com.example.board.domain.comment.CommentDto;
import com.example.board.domain.post.PostEntity;
import com.example.board.domain.user.UserEntity;
import com.example.board.persistence.CommentRepository;
import com.example.board.persistence.PostRepository;
import com.example.board.persistence.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Long createUser(String username, String nickname, String email) {
        UserEntity user = UserEntity.builder()
                .username(username)
                .password("1234")
                .nickname(nickname)
                .email(email)
                .build();

        return userRepository.save(user).getId();
    }

    private Long createPost(Long userId, String title, String content) {
        PostEntity post = PostEntity.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .build();

        return postRepository.save(post).getId();
    }

    @Test
    @DisplayName("CommentService 빈 주입 테스트")
    void testExists() {
        assertNotNull(commentService);
        log.info("commentService = {}", commentService);
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void testCreate() {
        Long userId = createUser("user1", "nick1", "user1@test.com");
        Long postId = createPost(userId, "게시글1", "내용1");

        CommentDto requestDto = CommentDto.builder()
                .postId(postId)
                .content("첫 댓글입니다.")
                .build();

        Optional<CommentDto> result = commentService.create(requestDto, userId);

        assertTrue(result.isPresent());
        assertThat(result.get().getContent()).isEqualTo("첫 댓글입니다.");
        assertThat(result.get().getPostId()).isEqualTo(postId);
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getWriter()).isEqualTo("nick1");

        PostEntity post = postRepository.findById(postId).orElseThrow();
        assertThat(post.getCommentsCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 작성 실패 - 로그인 사용자 없음")
    void testCreateFailWhenUserIdIsNull() {
        Long userId = createUser("user2", "nick2", "user2@test.com");
        Long postId = createPost(userId, "게시글2", "내용2");

        CommentDto requestDto = CommentDto.builder()
                .postId(postId)
                .content("댓글")
                .build();

        Optional<CommentDto> result = commentService.create(requestDto, null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("댓글 조회 성공")
    void testRead() {
        Long userId = createUser("user3", "nick3", "user3@test.com");
        Long postId = createPost(userId, "게시글3", "내용3");

        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("조회용 댓글")
                        .build(),
                userId
        ).orElseThrow();

        Optional<CommentDto> result = commentService.read(created.getId());

        assertTrue(result.isPresent());
        assertThat(result.get().getId()).isEqualTo(created.getId());
        assertThat(result.get().getContent()).isEqualTo("조회용 댓글");
        assertThat(result.get().getWriter()).isEqualTo("nick3");
    }

    @Test
    @DisplayName("댓글 수정 성공 - 작성자")
    void testUpdateSuccess() {
        Long userId = createUser("user4", "nick4", "user4@test.com");
        Long postId = createPost(userId, "게시글4", "내용4");

        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("수정 전 댓글")
                        .build(),
                userId
        ).orElseThrow();

        CommentDto updateDto = CommentDto.builder()
                .id(created.getId())
                .content("수정 후 댓글")
                .build();

        Optional<CommentDto> result = commentService.update(updateDto, userId);

        assertTrue(result.isPresent());
        assertThat(result.get().getContent()).isEqualTo("수정 후 댓글");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자 아님")
    void testUpdateFailWhenNotOwner() {
        Long ownerId = createUser("user5", "nick5", "user5@test.com");
        Long otherUserId = createUser("user6", "nick6", "user6@test.com");
        Long postId = createPost(ownerId, "게시글5", "내용5");

        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("원래 댓글")
                        .build(),
                ownerId
        ).orElseThrow();

        CommentDto updateDto = CommentDto.builder()
                .id(created.getId())
                .content("남이 수정한 댓글")
                .build();

        Optional<CommentDto> result = commentService.update(updateDto, otherUserId);

        assertTrue(result.isEmpty());

        CommentDto readDto = commentService.read(created.getId()).orElseThrow();
        assertThat(readDto.getContent()).isEqualTo("원래 댓글");
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 작성자")
    void testDeleteSuccess() {
        Long userId = createUser("user7", "nick7", "user7@test.com");
        Long postId = createPost(userId, "게시글6", "내용6");

        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("삭제할 댓글")
                        .build(),
                userId
        ).orElseThrow();

        PostEntity beforePost = postRepository.findById(postId).orElseThrow();
        assertThat(beforePost.getCommentsCount()).isEqualTo(1);

        boolean result = commentService.delete(created.getId(), userId);

        assertTrue(result);
        assertThat(commentService.read(created.getId())).isEmpty();

        PostEntity afterPost = postRepository.findById(postId).orElseThrow();
        assertThat(afterPost.getCommentsCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 작성자 아님")
    void testDeleteFailWhenNotOwner() {
        Long ownerId = createUser("user8", "nick8", "user8@test.com");
        Long otherUserId = createUser("user9", "nick9", "user9@test.com");
        Long postId = createPost(ownerId, "게시글7", "내용7");

        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("삭제 실패 댓글")
                        .build(),
                ownerId
        ).orElseThrow();

        boolean result = commentService.delete(created.getId(), otherUserId);

        assertFalse(result);
        assertThat(commentService.read(created.getId())).isPresent();

        PostEntity post = postRepository.findById(postId).orElseThrow();
        assertThat(post.getCommentsCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 게시글 댓글 목록 조회 - 최신순 페이징")
    void testGetList() {
        Long userId = createUser("user10", "nick10", "user10@test.com");
        Long postId = createPost(userId, "게시글8", "내용8");

        commentService.create(CommentDto.builder().postId(postId).content("댓글1").build(), userId);
        commentService.create(CommentDto.builder().postId(postId).content("댓글2").build(), userId);
        commentService.create(CommentDto.builder().postId(postId).content("댓글3").build(), userId);

        Page<CommentDto> page = commentService.getList(
                postId,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertNotNull(page);
        assertThat(page.getContent()).hasSize(3);

        // 최신순이므로 마지막에 저장한 댓글3이 먼저 나와야 함
        assertThat(page.getContent().get(0).getContent()).isEqualTo("댓글3");
        assertThat(page.getContent().get(1).getContent()).isEqualTo("댓글2");
        assertThat(page.getContent().get(2).getContent()).isEqualTo("댓글1");
    }

    @Test
    @DisplayName("전체 댓글 목록 조회")
    void testGetAllComments() {
        Long userId = createUser("user11", "nick11", "user11@test.com");
        Long postId1 = createPost(userId, "게시글9", "내용9");
        Long postId2 = createPost(userId, "게시글10", "내용10");

        commentService.create(CommentDto.builder().postId(postId1).content("전체댓글1").build(), userId);
        commentService.create(CommentDto.builder().postId(postId2).content("전체댓글2").build(), userId);

        Page<CommentDto> page = commentService.getAllComments(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertNotNull(page);
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("댓글 작성 실패 - 내용 없음")
    void testCreateFailWhenContentIsBlank() {
        Long userId = createUser("user12", "nick12", "user12@test.com");
        Long postId = createPost(userId, "게시글11", "내용11");

        CommentDto requestDto = CommentDto.builder()
                .postId(postId)
                .content(" ")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.create(requestDto, userId)
        );

        assertThat(exception.getMessage()).isEqualTo("내용을 입력해주세요.");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 200자 초과")
    void testUpdateFailWhenContentTooLong() {
        Long userId = createUser("user13", "nick13", "user13@test.com");
        Long postId = createPost(userId, "게시글12", "내용12");

        CommentDto created = commentService.create(
                CommentDto.builder()
                        .postId(postId)
                        .content("정상 댓글")
                        .build(),
                userId
        ).orElseThrow();

        String longContent = "a".repeat(201);

        CommentDto updateDto = CommentDto.builder()
                .id(created.getId())
                .content(longContent)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.update(updateDto, userId)
        );

        assertThat(exception.getMessage()).isEqualTo("최대 200자까지 입력 가능합니다.");
    }
}