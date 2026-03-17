package com.example.board.persistence;

import com.example.board.domain.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testExists() {
        assertNotNull(userRepository);
        log.info("UserRepository Test = {}" , userRepository);
    }

    // 저장 테스트
    @Test
    public void testSave(){
        UserEntity user = UserEntity.builder()
                .username("hong")
                .password("abc1234!")
                .nickname("gildong")
                .email("hong@naver.com")
                .build();
        log.info("Before save : user = {}", user);

        UserEntity savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertThat(savedUser.getUsername()).isEqualTo("hong");
        log.info("After save : user = {}", savedUser);
    }

    //Helper method
    private UserEntity createUser(String username, String name, String nickname, String email) {
        return UserEntity.builder()
                .username(username)
                .password("abc1234!")
                .nickname(nickname)
                .email(email)
                .build();
    }
}