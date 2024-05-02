package com.unity.goods.domain.member.service;

import static com.unity.goods.domain.member.type.Status.ACTIVE;
import static com.unity.goods.domain.member.type.Status.INACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unity.goods.domain.member.dto.SignUpDto.SignUpRequest;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class MemberServiceTest {

  @Autowired
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  public void signUpTestWithNoImage() {
    // given
    SignUpRequest signUpRequest = SignUpRequest.builder()
        .email("fortestseowon@gmail.com")
        .password("12345678")
        .chkPassword("12345678")
        .nickName("nickName")
        .tradePassword("123456")
        .build();

    // when
    memberService.signUp(signUpRequest);

    // then
    Optional<Member> byEmail = memberRepository.findByEmail("fortestseowon@gmail.com");
    if (byEmail.isPresent()) {
      Member member = byEmail.get();
      assertEquals(signUpRequest.getNickName(), member.getNickname());
      assertTrue(passwordEncoder.matches("12345678", member.getPassword()));
      assertTrue(passwordEncoder.matches("123456", member.getTradePassword()));
    }

  }

  @Test
  @DisplayName("회원 탈퇴 상태 변경")
  public void resignTest() {

    String originalPw = "12345678";
    String encode = passwordEncoder.encode(originalPw);
    // given
    Member member = Member.builder()
        .email("fortestseowon@gmail.com")
        .password(encode)
        .nickname("nickName")
        .status(ACTIVE)
        .tradePassword("123456")
        .build();

    memberRepository.save(member);

    // when
//    memberService.resign(member, ResignRequest.builder().password(originalPw).build());

    // then
    Optional<Member> byEmail = memberRepository.findByEmail(member.getEmail());
    if(byEmail.isEmpty()){
      throw new MemberException(ErrorCode.USER_NOT_FOUND);
    }

    Member savedMember = byEmail.get();
    assertEquals(INACTIVE, savedMember.getStatus());
  }


}