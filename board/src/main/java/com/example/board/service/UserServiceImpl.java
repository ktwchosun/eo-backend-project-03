package com.example.board.service;


import com.example.board.domain.user.UserDto;
import com.example.board.domain.user.UserEntity;
import com.example.board.persistence.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void create(@NotNull UserDto userDto){
        log.info("signup start : username = {}", userDto.getUsername());

        //중복 체크
        checkUsernameAvailability(userDto.getUsername());
        checkNicknameAvailability(userDto.getNickname());
        checkEmailAvailability(userDto.getEmail());

        //비밀 번호 암호화
        setEncodedPassword(userDto);

        // 엔티티 변환 및 저장
        UserEntity userEntity = UserEntity.builder()
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .nickname(userDto.getNickname())
                .email(userDto.getEmail())
                .build();

        UserEntity savedEntity = userRepository.save(userEntity);
        userDto.setId(userEntity.getId());

        log.info("signup end : username = {}", savedEntity.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserDto> read(@NotNull Long id) {
        return userRepository.findById(id).map(UserDto::from);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserDto> read(@NotNull String username) {
        return userRepository.findByUsername(username).map(UserDto::from);
    }

    // 회원 탈퇴
    @Override
    public boolean delete(@NotNull Long id) {
        log.info("delete: id={}", id);

        return userRepository.findById(id).map(userEntity -> {

            userRepository.delete(userEntity);


            log.info("회원 탈퇴 완료: id={}", id);
            return true;
        }).orElse(false);
    }


    // 아이디 사용 가능 여부 확인
    private void checkUsernameAvailability(@NotNull String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already in use");
        }
    }

    // 닉네임 사용 가능 여부 확인
    private void checkNicknameAvailability(@NotNull String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("Nickname is already in use");
        }
    }

    // 이메일 사용 가능 여부 확인
    private void checkEmailAvailability(@NotNull String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }
    }

    // 비밀번호 암호화
    private UserDto setEncodedPassword(@NotNull UserDto userDto) {
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);
        return userDto;
    }

    // 아아디 중복체크
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // 닉네임 중복 체크
    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}
