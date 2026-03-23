package com.example.board.service;

import com.example.board.domain.post.PostDto;
import com.example.board.domain.post.PostEntity;
import com.example.board.domain.user.UserEntity;
import com.example.board.persistence.PostRepository;
import com.example.board.persistence.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private Long createUser(String username, String nickname, String email) {
        UserEntity user = UserEntity.builder()
                .username(username)
                .password("1234")
                .nickname(nickname)
                .email(email)
                .build();

        return userRepository.save(user).getId();
    }

    @Test
    void testExists() {
        assertNotNull(postService);
        log.info("postService = {}", postService);
    }

    @Test
    void testCreate() {
        Long userId = createUser("user1", "nick1", "user1@test.com");

        PostDto postDto = PostDto.builder()
                .title("[TEST] PostServiceTest#testCreate")
                .content("[TEST] PostServiceTest#testCreate")
                .build();

        Long createdId = postService.create(postDto, userId);

        assertNotNull(createdId);
        assertThat(createdId).isGreaterThan(0L);

        PostEntity savedPost = postRepository.findById(createdId).orElseThrow();
        assertThat(savedPost.getTitle()).isEqualTo(postDto.getTitle());
        assertThat(savedPost.getContent()).isEqualTo(postDto.getContent());
        assertThat(savedPost.getUserId()).isEqualTo(userId);
    }

    @Test
    void testRead() {
        Long userId = createUser("user2", "nick2", "user2@test.com");

        Long postId = postService.create(
                PostDto.builder()
                        .title("[TEST] PostServiceTest#testRead")
                        .content("read content")
                        .build(),
                userId
        );

        int before = postRepository.findById(postId).orElseThrow().getViewCount();

        PostDto postDto = postService.read(postId);

        assertNotNull(postDto);
        assertThat(postDto.getId()).isEqualTo(postId);

        int after = postRepository.findById(postId).orElseThrow().getViewCount();
        assertThat(after).isEqualTo(before + 1);
    }

    @Test
    void testUpdate() {
        Long userId = createUser("user3", "nick3", "user3@test.com");

        Long postId = postService.create(
                PostDto.builder()
                        .title("[TEST] before update")
                        .content("before update")
                        .build(),
                userId
        );

        PostDto updateDto = PostDto.builder()
                .title("[TEST] PostServiceTest#testUpdate")
                .content("update content")
                .build();

        boolean result = postService.update(postId, updateDto, userId);

        assertTrue(result);

        PostEntity updated = postRepository.findById(postId).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("[TEST] PostServiceTest#testUpdate");
        assertThat(updated.getContent()).isEqualTo("update content");
    }

    @Test
    void testUpdate_notOwner_fail() {
        Long ownerId = createUser("user4", "nick4", "user4@test.com");
        Long otherUserId = createUser("user5", "nick5", "user5@test.com");

        Long postId = postService.create(
                PostDto.builder()
                        .title("owner title")
                        .content("owner content")
                        .build(),
                ownerId
        );

        PostDto updateDto = PostDto.builder()
                .title("hacked title")
                .content("hacked content")
                .build();

        boolean result = postService.update(postId, updateDto, otherUserId);

        assertFalse(result);

        PostEntity notUpdated = postRepository.findById(postId).orElseThrow();
        assertThat(notUpdated.getTitle()).isEqualTo("owner title");
        assertThat(notUpdated.getContent()).isEqualTo("owner content");
    }

    @Test
    void testDelete_owner_success() {
        Long userId = createUser("user6", "nick6", "user6@test.com");

        Long postId = postService.create(
                PostDto.builder()
                        .title("[TEST] delete owner")
                        .content("delete owner")
                        .build(),
                userId
        );

        boolean result = postService.delete(postId, userId);

        assertTrue(result);
        assertThat(postRepository.findById(postId)).isEmpty();
    }

    @Test
    void testDelete_notOwner_fail() {
        Long ownerId = createUser("user7", "nick7", "user7@test.com");
        Long otherUserId = createUser("user8", "nick8", "user8@test.com");

        Long postId = postService.create(
                PostDto.builder()
                        .title("t")
                        .content("c")
                        .build(),
                ownerId
        );

        boolean result = postService.delete(postId, otherUserId);

        assertFalse(result);
        assertThat(postRepository.findById(postId)).isPresent();
    }

    @Test
    void testGetAllPosts() {
        Long userId = createUser("user9", "nick9", "user9@test.com");

        postService.create(
                PostDto.builder()
                        .title("[TEST] PostServiceTest#testGetAllPosts - 1")
                        .content("전체 조회 테스트 1")
                        .build(),
                userId
        );

        postService.create(
                PostDto.builder()
                        .title("[TEST] PostServiceTest#testGetAllPosts - 2")
                        .content("전체 조회 테스트 2")
                        .build(),
                userId
        );

        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<PostDto> result = postService.getAllPosts(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().size()).isLessThanOrEqualTo(10);

        result.getContent().forEach(post -> {
            assertThat(post.getWriter()).isNotNull();
            log.info("게시글 = {}, 작성자 = {}", post.getTitle(), post.getWriter());
        });
    }

    @Test
    void testPreviousAndNextPost() {
        Long userId = createUser("user10", "nick10", "user10@test.com");

        Long firstId = postService.create(
                PostDto.builder().title("first").content("first").build(),
                userId
        );

        Long secondId = postService.create(
                PostDto.builder().title("second").content("second").build(),
                userId
        );

        var previousPost = postService.getPreviousPost(firstId);
        var nextPost = postService.getNextPost(secondId);

        assertThat(previousPost).isPresent();
        assertThat(previousPost.get().getId()).isEqualTo(secondId);

        assertThat(nextPost).isPresent();
        assertThat(nextPost.get().getId()).isEqualTo(firstId);
    }
}