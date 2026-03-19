package com.example.board.persistence;

import com.example.board.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    //중복 체크
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    // 조회
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByNickname(String nickname);
    Optional<UserEntity> findByEmail(String email);

}
