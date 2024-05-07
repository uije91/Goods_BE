package com.unity.goods.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unity.goods.domain.member.dto.FindPasswordDto.FindPasswordRequest;
import com.unity.goods.domain.member.dto.LoginDto.LoginRequest;
import com.unity.goods.domain.member.service.MemberService;
import com.unity.goods.global.security.WithCustomMockUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  MemberService memberService;


  @Test
  @DisplayName("로그인 성공")
  @WithMockUser
  void login_success() throws Exception {
    String email = "test@test.com";
    String password = "!q2w3e4r";

    LoginRequest login = LoginRequest.builder().email(email).password(password).build();

    mockMvc.perform(post("/api/member/login")
            .with(csrf())
            .content(objectMapper.writeValueAsString(login))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(print());
  }


  @Test
  @WithCustomMockUser
  @DisplayName("비밀번호 찾기 테스트")
  void findPassword() throws Exception {

    Mockito.doNothing().when(memberService)
        .findPassword(any(FindPasswordRequest.class));

    mockMvc.perform(
            post("/api/member/find")
                .with(csrf())
        )
        .andExpect(status().isOk())
        .andDo(print());
  }
}
