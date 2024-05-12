package com.unity.goods.domain.member.service;

import static com.unity.goods.domain.member.type.Status.ACTIVE;
import static com.unity.goods.domain.member.type.Status.RESIGN;
import static com.unity.goods.global.exception.ErrorCode.RESIGNED_ACCOUNT;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unity.goods.domain.member.dto.ChangePasswordDto.ChangePasswordRequest;
import com.unity.goods.domain.member.dto.ChangeTradePasswordDto.ChangeTradePasswordRequest;
import com.unity.goods.domain.member.dto.FindPasswordDto.FindPasswordRequest;
import com.unity.goods.domain.member.dto.MemberProfileDto.MemberProfileResponse;
import com.unity.goods.domain.member.dto.ResignDto.ResignRequest;
import com.unity.goods.domain.member.dto.SignUpDto.SignUpRequest;
import com.unity.goods.domain.member.entity.Badge;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.BadgeRepository;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.BadgeType;
import com.unity.goods.domain.member.type.Role;
import com.unity.goods.domain.member.type.SocialType;
import com.unity.goods.domain.member.type.Status;
import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.global.exception.ErrorCode;
import com.unity.goods.global.jwt.JwtTokenProvider;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.infra.service.RedisService;
import io.jsonwebtoken.lang.Assert;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@Transactional
class MemberServiceTest {

  @InjectMocks
  private MemberService memberService;

  @Mock
  private MemberRepository memberRepository;
  @Mock
  private BadgeRepository badgeRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private RedisService redisService;
  @Mock
  private JwtTokenProvider jwtTokenProvider;
  @Mock
  private MailSender mailSender;

  private Member member;

  @BeforeEach
  public void setMember() {
    member = Member.builder()
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
  @DisplayName("회원 가입 - 이미지 X 경우(s3 업로드 고려 X) 테스트")
  public void signUpTestWithNoImage() {

    String fakeMemberEmail = "fortestseowon@gmail2.com";
    String encodedPw = "12345678", encodedTradePw = "123456";

    Member member = Member.builder()
        .email(fakeMemberEmail)
        .password(encodedPw)
        .nickname("nickName")
        .status(ACTIVE)
        .tradePassword(encodedTradePw)
        .build();

    SignUpRequest mockSignUpRequest = SignUpRequest.builder()
        .email(fakeMemberEmail)
        .password(encodedPw)
        .chkPassword(encodedPw)
        .nickName("nickName")
        .tradePassword(encodedTradePw)
        .build();

    // given
    given(memberRepository.existsByEmail(mockSignUpRequest.getEmail())).willReturn(false);
    given(memberRepository.existsByNickname(mockSignUpRequest.getNickName())).willReturn(false);
    given(passwordEncoder.encode(mockSignUpRequest.getPassword())).willReturn(encodedPw);
    given(passwordEncoder.encode(mockSignUpRequest.getTradePassword())).willReturn(encodedTradePw);

    given(memberRepository.findByEmail(mockSignUpRequest.getEmail()))
        .willReturn(Optional.of(member));

    // when
    memberService.signUp(mockSignUpRequest);

    // then
    Member signUpMember = memberRepository.findByEmail(fakeMemberEmail)
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    assertEquals(mockSignUpRequest.getNickName(), signUpMember.getNickname());
    assertEquals(ACTIVE, signUpMember.getStatus());
    assertEquals(encodedPw, signUpMember.getPassword());
    assertEquals(encodedTradePw, signUpMember.getTradePassword());
  }

  @Test
  @DisplayName("회원 탈퇴 상태 변경")
  public void resignTest() {

    String encodedPw = "12345678";
    // given
    String fakeMemberEmail = "fortestseowon@gmail2.com";
    Member member = Member.builder()
        .id(1L)
        .email(fakeMemberEmail)
        .password(encodedPw)
        .nickname("nickName")
        .status(ACTIVE)
        .tradePassword("123456")
        .build();

    memberRepository.save(member);
    ResignRequest resignRequest = ResignRequest.builder().password(encodedPw).build();

    // given
    given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
    given(passwordEncoder.matches(resignRequest.getPassword(), member.getPassword())).willReturn(
        true);
    given(redisService.getData("RT:" + member.getEmail())).willReturn("");
    given(jwtTokenProvider.getTokenExpirationTime(member.getEmail())).willReturn(1L);

    // when
    UserDetailsImpl userDetails = new UserDetailsImpl(member);
    memberService.resign(fakeMemberEmail, userDetails, resignRequest);

    // then
    Member resignedMember = memberRepository.findByEmail(member.getEmail())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    assertEquals(RESIGN, resignedMember.getStatus());
  }

  @Test
  @DisplayName("토큰 재발급 실패 - 잘못된 토큰")
  void reissue_fail_invalidToken() {
    // given
    String accessToken = "validAccessToken";
    String refreshToken = "invalidRefreshToken";

    TokenDto tokenDto = new TokenDto(accessToken, refreshToken);

    when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);

    // when
    MemberException memberException = assertThrows(MemberException.class,
        () -> memberService.reissue(tokenDto));

    // then
    assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, memberException.getErrorCode());
  }

  @Test
  @DisplayName("비밀번호 찾기 이메일 생성 테스트")
  void findPasswordEmailTest() {
    //given
    given(memberRepository.findByEmail(anyString()))
        .willReturn(Optional.of(Member.builder()
            .email("test@naver.com")
            .password("test1234")
            .build()));

    doNothing().when(mailSender).send(any(SimpleMailMessage.class));

    //when
    memberService.findPassword(
        FindPasswordRequest.builder()
            .email("test@naver.com")
            .build());

    //then
    verify(memberRepository, times(1)).findByEmail(anyString());

  }

  @Test
  @DisplayName("회원 프로필 조회 성공 테스트")
  void getMemberProfileSuccess() {
    //given
    Member member = Member.builder()
        .email("test@naver.com")
        .password("test1234")
        .nickname("test")
        .status(ACTIVE)
        .phoneNumber("010-1111-1111")
        .profileImage("http://amazonS3/test.jpg")
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(member);

    given(memberRepository.findByEmail(anyString()))
        .willReturn(Optional.of(member));

    //when
    MemberProfileResponse memberProfile = memberService.getMemberProfile(userDetails);

    //then
    assertEquals("test", memberProfile.getNickName());
    assertEquals("010-1111-1111", memberProfile.getPhoneNumber());
    assertEquals("http://amazonS3/test.jpg", memberProfile.getProfileImage());
    assertEquals(0.0, memberProfile.getStar());
  }

  @Test
  @DisplayName("탈퇴 회원 프로필 조회 실패 테스트")
  void getMemberProfileFail() {
    //given
    Member member = Member.builder()
        .email("test@naver.com")
        .password("test1234")
        .nickname("test")
        .status(RESIGN)
        .phoneNumber("010-1111-1111")
        .profileImage("http://amazonS3/test.jpg")
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(member);

    given(memberRepository.findByEmail(anyString()))
        .willReturn(Optional.of(member));

    //when
    MemberException exception = assertThrows(MemberException.class, ()
        -> memberService.getMemberProfile(userDetails));

    //then
    assertEquals(RESIGNED_ACCOUNT, exception.getErrorCode());
  }

  @Test
  @DisplayName("비밀번호 변경 테스트")
  void changePasswordTest() {
    //given
    Member member = Member.builder()
        .email("test@naver.com")
        .password("test1234")
        .nickname("test")
        .status(RESIGN)
        .phoneNumber("010-1111-1111")
        .profileImage("http://amazonS3/test.jpg")
        .build();

    ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
        .curPassword("test1234")
        .newPassword("new1234")
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(member);

    given(memberRepository.findByEmail(anyString()))
        .willReturn(Optional.of(member));
    given(passwordEncoder.encode(changePasswordRequest.getNewPassword())).willReturn("new1234");
    given(!passwordEncoder.matches(changePasswordRequest.getCurPassword(),
        member.getPassword())).willReturn(true);

    //when
    memberService.changePassword(changePasswordRequest, userDetails);

    //then
    assertEquals(member.getPassword(), "new1234");
  }

  @Test
  @DisplayName("거래 비밀번호 변경 테스트")
  void changeTradePasswordTest() {
    //given
    Member member = Member.builder()
        .email("test@naver.com")
        .password("test1234")
        .tradePassword("111111")
        .nickname("test")
        .status(RESIGN)
        .phoneNumber("010-1111-1111")
        .profileImage("http://amazonS3/test.jpg")
        .build();

    ChangeTradePasswordRequest changeTradePasswordRequest = ChangeTradePasswordRequest.builder()
        .curTradePassword("111111")
        .newTradePassword("222222")
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(member);

    given(memberRepository.findByEmail(anyString()))
        .willReturn(Optional.of(member));
    given(passwordEncoder.encode(changeTradePasswordRequest.getNewTradePassword())).willReturn(
        "222222");
    given(!passwordEncoder.matches(changeTradePasswordRequest.getCurTradePassword(),
        member.getTradePassword())).willReturn(true);

    //when
    memberService.changeTradePassword(changeTradePasswordRequest, userDetails);

    //then
    assertEquals(member.getTradePassword(), "222222");
  }

  @Test
  @DisplayName("배지 조회 성공")
  void getBadgeTest() {
    //given
    List<Badge> badgeList = Arrays.asList(
        new Badge(1L, member, BadgeType.SELL),
        new Badge(2L, member, BadgeType.MANNER)
    );

    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(badgeRepository.findAllByMember_Id(member.getId())).thenReturn(badgeList);

    //when
    List<BadgeType> result = memberService.getBadge(member.getId());

    //then
    assertEquals(2,result.size());
    assertTrue(result.contains(BadgeType.SELL));
    assertTrue(result.contains(BadgeType.MANNER));
  }

}