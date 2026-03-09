package com.example.board.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity

public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * 아이디 (로그인, 중복 불가)
     */
    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 50, message = "아이디는 4자 이상 50자 이하여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문, 숫자, 언더바만 사용 가능합니다")
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    /**
     * 비밀번호 (암호화 저장)
     */
    @NotBlank(message = "비밀번호는 필수입니다")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 닉네임 (중복 불가)
     */
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다")
    @Column(name = "nickname", length = 50, nullable = false, unique = true)
    private String nickname;

    /**
     * 이메일 (중복 불가)
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Size(min = 6, max = 100, message = "이메일은 6자 이상 100자 이하여야 합니다")
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "올바른 이메일 형식이 아닙니다")
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;


    //  생성자 (Builder)

    @Builder
    public UserEntity(String username, String password, String nickname, String email) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
    }
}
