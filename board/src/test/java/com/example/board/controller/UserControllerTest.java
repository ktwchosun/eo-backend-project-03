package com.example.board.controller;

import com.example.board.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("로그인 페이지 이동")
    void loginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("회원가입 페이지 이동")
    void signupPage() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("userDto"));
    }

    @Test
    @DisplayName("회원가입 성공")
    void signupSuccess() throws Exception {
        willDoNothing().given(userService).create(any());

        mockMvc.perform(post("/signup")
                        .param("username", "testuser")
                        .param("password", "1234")
                        .param("nickname", "tester")
                        .param("email", "test@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("회원가입 실패 - validation 오류")
    void signupFailValidation() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("username", "")
                        .param("password", "")
                        .param("nickname", "")
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    @DisplayName("회원가입 실패 - 서비스 예외")
    void signupFailByServiceException() throws Exception {
        doThrow(new IllegalArgumentException("이미 사용 중인 아이디입니다."))
                .when(userService).create(any());

        mockMvc.perform(post("/signup")
                        .param("username", "duplicateUser")
                        .param("password", "1234")
                        .param("nickname", "tester")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "이미 사용 중인 아이디입니다."));
    }

    @Test
    @DisplayName("아이디 중복 체크 - 사용 가능")
    void checkUsernameAvailable() throws Exception {
        given(userService.existsByUsername("newuser")).willReturn(false);

        mockMvc.perform(get("/check-username")
                        .param("username", "newuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("아이디 중복 체크 - 이미 사용 중")
    void checkUsernameDuplicated() throws Exception {
        given(userService.existsByUsername("existuser")).willReturn(true);

        mockMvc.perform(get("/check-username")
                        .param("username", "existuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 사용 가능")
    void checkNicknameAvailable() throws Exception {
        given(userService.existsByNickname("newnick")).willReturn(false);

        mockMvc.perform(get("/check-nickname")
                        .param("nickname", "newnick"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 이미 사용 중")
    void checkNicknameDuplicated() throws Exception {
        given(userService.existsByNickname("existnick")).willReturn(true);

        mockMvc.perform(get("/check-nickname")
                        .param("nickname", "existnick"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("비밀번호 찾기 페이지 이동")
    void findPasswordPage() throws Exception {
        mockMvc.perform(get("/findpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("findpassword"));
    }
}