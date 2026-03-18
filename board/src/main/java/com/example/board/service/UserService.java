package com.example.board.service;

import com.example.board.domain.user.UserDto;

import java.util.Optional;

public interface UserService {
    void create(UserDto userDto);
    Optional<UserDto> read(Long id);
    Optional<UserDto> read(String username);
    boolean delete(Long id);

    /*
    아이디 중복 체크
     */
    boolean existsByUsername(String username);

    /*
    닉네임 중복 체크
     */
    boolean existsByNickname(String nickname);
}
