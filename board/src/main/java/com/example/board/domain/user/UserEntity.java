package com.example.board.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 아이디 (로그인, 중복 불가)
     */
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    /**
     * 비밀번호 (암호화 저장)
     */
    @Column(name = "password", length = 100, nullable = false)
    private String password;

    /**
     * 닉네임 (중복 불가)
     */
    @Column(name = "nickname", length = 50, nullable = false, unique = true)
    private String nickname;

    /**
     * 이메일 (중복 불가)
     */
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Builder
    public UserEntity(String username, String password, String nickname, String email) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
}
