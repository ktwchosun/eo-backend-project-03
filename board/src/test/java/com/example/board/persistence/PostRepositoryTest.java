package com.example.board.persistence;

import com.example.board.domain.post.PostEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    private final Long USER_ID = 1L;

    @Test
    void testExists() {
        assertNotNull(postRepository);
        log.info("postRepository = {}", postRepository);
    }

    @Test
    void testGetList() {
        var postEntityList = postRepository.findAll();
        log.info("postEntityList.size() = {}", postEntityList.size());
        assertNotNull(postEntityList);
    }

    @Test
    void testGetListWithPaging() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<PostEntity> postEntityPage = postRepository.findAll(pageable);

        assertNotNull(postEntityPage);
        assertEquals(10, postEntityPage.getSize());
        assertEquals(0, postEntityPage.getNumber());

        log.info("Page.getTotalElements() = {}", postEntityPage.getTotalElements());
        log.info("Page.getTotalPages() = {}", postEntityPage.getTotalPages());
        log.info("Page.getNumber() = {}", postEntityPage.getNumber());
        log.info("Page.getSize() = {}", postEntityPage.getSize());
    }

    @Test
    void testCreate() {
        String title = "[TEST] PostRepositoryTest#testCreate";

        PostEntity postEntity = PostEntity.builder()
                .userId(USER_ID)
                .title(title)
                .content(title)
                .build();

        log.info("postEntity(before save) = {}", postEntity);

        PostEntity savedEntity = postRepository.save(postEntity);

        assertNotNull(savedEntity);
        assertNotNull(savedEntity.getId());
        assertEquals(title, savedEntity.getTitle());

        log.info("savedEntity = {}", savedEntity);
    }

    @Test
    void testRead() {
        PostEntity saved = postRepository.save(
                PostEntity.builder()
                        .userId(USER_ID)
                        .title("[TEST] PostRepositoryTest#testRead")
                        .content("content")
                        .build()
        );

        Long id = saved.getId();

        postRepository.findById(id).ifPresentOrElse(
                postEntity -> {
                    assertEquals(id, postEntity.getId());
                    log.info("postEntity = {}", postEntity);
                },
                () -> { throw new RuntimeException("조회한 게시글이 없습니다."); }
        );
    }

    @Test
    void testUpdate() {
        PostEntity saved = postRepository.save(
                PostEntity.builder()
                        .userId(USER_ID)
                        .title("[TEST] before update")
                        .content("before update")
                        .build()
        );

        Long id = saved.getId();
        String newTitle = "[TEST] PostRepositoryTest#testUpdate";
        String newContent = "updated content";

        PostEntity postEntity = postRepository.findById(id).orElseThrow();
        log.info("postEntity(before) = {}", postEntity);

        postEntity.updatePost(newTitle, newContent);

        PostEntity updatedEntity = postRepository.save(postEntity);

        assertNotNull(updatedEntity);
        assertEquals(newTitle, updatedEntity.getTitle());
        assertEquals(newContent, updatedEntity.getContent());

        log.info("updatedEntity = {}", updatedEntity);
    }

    @Test
    void testDelete() {
        PostEntity saved = postRepository.save(
                PostEntity.builder()
                        .userId(USER_ID)
                        .title("[TEST] PostRepositoryTest#testDelete")
                        .content("delete content")
                        .build()
        );

        Long id = saved.getId();
        final long countBefore = postRepository.count();
        log.info("countBefore = {}", countBefore);

        postRepository.findById(id).ifPresent(postEntity -> {
            postRepository.delete(postEntity);
            log.info("deletedEntity = {}", postEntity);

            final long countAfter = postRepository.count();
            log.info("countAfter = {}", countAfter);

            assertEquals(countBefore - 1, countAfter);
            assertTrue(postRepository.findById(id).isEmpty());
        });
    }
}