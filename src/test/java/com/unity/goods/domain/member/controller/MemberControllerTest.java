package com.unity.goods.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unity.goods.domain.member.dto.ChangePasswordDto.ChangePasswordRequest;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.service.MemberService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.global.security.WithCustomMockUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

  @MockBean
  MemberService memberService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithCustomMockUser
  @DisplayName("비밀번호 변경 테스트")
  void changePassword() throws Exception {

    doNothing().when(memberService)
        .changePassword(any(ChangePasswordRequest.class), any(UserDetailsImpl.class));

    mockMvc.perform(
            put("/api/member/change")
                .with(csrf())
        )
        .andExpect(status().isOk())
        .andDo(print());

  }


}