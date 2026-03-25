package com.example.board.service;

import com.example.board.domain.user.UserDto;
import com.example.board.domain.user.UserEntity;
import com.example.board.persistence.UserRepository;
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
    public void create(UserDto userDto) {
        log.info("signup start: username={}", userDto.getUsername());

        validateUserDto(userDto);

        checkUsernameAvailability(userDto.getUsername());
        checkNicknameAvailability(userDto.getNickname());
        checkEmailAvailability(userDto.getEmail());

        String encodedPassword = encodePassword(userDto.getPassword());

        UserEntity userEntity = UserEntity.builder()
                .username(userDto.getUsername())
                .password(encodedPassword)
                .nickname(userDto.getNickname())
                .email(userDto.getEmail())
                .build();

        UserEntity savedEntity = userRepository.save(userEntity);
        userDto.setId(savedEntity.getId());

        log.info("signup end: userId={}, username={}", savedEntity.getId(), savedEntity.getUsername());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserDto> read(Long id) {
        return userRepository.findById(id)
                .map(UserDto::from);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserDto> read(String username) {
        return userRepository.findByUsername(username)
                .map(UserDto::from);
    }

    @Override
    public boolean delete(Long id) {
        log.info("delete start: id={}", id);

        return userRepository.findById(id)
                .map(userEntity -> {
                    userRepository.delete(userEntity);
                    log.info("delete success: id={}", id);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    private void validateUserDto(UserDto userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("회원 정보가 없습니다.");
        }

        if (userDto.getUsername() == null || userDto.getUsername().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        if (userDto.getNickname() == null || userDto.getNickname().isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
    }

    private void checkUsernameAvailability(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
    }

    private void checkNicknameAvailability(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }

    private void checkEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}