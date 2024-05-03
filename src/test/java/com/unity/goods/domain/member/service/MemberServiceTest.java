package com.unity.goods.domain.member.service;

import static com.unity.goods.domain.member.type.Status.ACTIVE;
import static com.unity.goods.domain.member.type.Status.RESIGN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unity.goods.domain.member.dto.FindPasswordDto.FindPasswordRequest;
import com.unity.goods.domain.member.dto.ResignDto.ResignRequest;
import com.unity.goods.domain.member.dto.SignUpDto.SignUpRequest;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.Role;
import com.unity.goods.domain.member.type.SocialType;
import com.unity.goods.domain.member.type.Status;
import com.unity.goods.global.exception.ErrorCode;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

  @Autowired
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  public void setMember() {
    Member member = Member.builder()
        .nickname("test")
        .email("test@naver.com")
        .password("test1234")
        .star(2.0)
        .role(Role.USER)
        .status(Status.INACTIVE)
        .tradePassword("1234")
        .socialType(SocialType.SERVER)
        .build();

    memberRepository.save(member);
  }

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
        .email("fortestseowon@gmail2.com")
        .password(encode)
        .nickname("nickName")
        .status(ACTIVE)
        .tradePassword("123456")
        .build();

    memberRepository.save(member);

    UserDetailsImpl userDetails = new UserDetailsImpl(member);

    // when
    memberService.resign(member.getEmail(), userDetails, ResignRequest.builder().password(originalPw).build());

    // then
    Optional<Member> byEmail = memberRepository.findByEmail(member.getEmail());
    if(byEmail.isEmpty()){
      throw new MemberException(ErrorCode.USER_NOT_FOUND);
    }

    Member savedMember = byEmail.get();
    assertEquals(RESIGN, savedMember.getStatus());
  }

  @Test
  @DisplayName("비밀번호 찾기 이메일 생성 테스트")
  void findPasswordEmailTest() {
    //given
    FindPasswordRequest findPasswordRequest = FindPasswordRequest.builder()
        .email("test@naver.com")
        .build();

    String tempPassword = "1a2B3%571!";

    //when
    SimpleMailMessage findPasswordEmail
        = memberService.createFindPasswordEmail(findPasswordRequest.getEmail(), tempPassword);

    //then
    Assertions.assertEquals(findPasswordEmail.getText(),
        "안녕하세요. 중고거래 마켓 " + "Goods" + "입니다."
            + "\n\n" + "임시 비밀번호는 [" + "1a2B3%571!" + "] 입니다.");

  }

}