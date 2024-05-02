package com.unity.goods.domain.member.service;

import com.unity.goods.domain.member.dto.ChangePasswordDto.ChangePasswordRequest;
import com.unity.goods.domain.member.dto.FindPasswordDto.FindPasswordRequest;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.Role;
import com.unity.goods.domain.member.type.SocialType;
import com.unity.goods.domain.member.type.Status;
import com.unity.goods.global.jwt.UserDetailsImpl;
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
  MemberService memberService;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

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
  @DisplayName("비밀번호 변경 테스트")
  void changePassword() {
    //given
    Member member = Member.builder()
        .nickname("test")
        .email("test@naver.com")
        .password("test12345")
        .star(2.0)
        .role(Role.USER)
        .status(Status.INACTIVE)
        .tradePassword("1234")
        .socialType(SocialType.SERVER)
        .build();

    ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
        .password("new1234")
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(member);

    //when
    memberService.changePassword(changePasswordRequest, userDetails);
    Member changePasswordMember = memberRepository.findByEmail(member.getEmail()).get();

    boolean validateChangePassword = passwordEncoder.matches(changePasswordRequest.getPassword(),
        changePasswordMember.getPassword());

    //then
    Assertions.assertTrue(validateChangePassword);


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