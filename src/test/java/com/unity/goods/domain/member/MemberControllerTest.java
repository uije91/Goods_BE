package com.unity.goods.domain.member;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unity.goods.domain.member.dto.LoginDto.LoginRequest;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.SocialType;
import com.unity.goods.domain.member.type.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest
public class MemberControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void login_test() throws Exception {
    String email = "test@test.com";
    String password = "1q2w3e4$";

    LoginRequest login = LoginRequest.builder()
        .email(email)
        .password(password)
        .build();

    mockMvc.perform(post("/api/member/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(login)))
        .andExpect(status().isOk())
        .andDo(print());
  }
}
