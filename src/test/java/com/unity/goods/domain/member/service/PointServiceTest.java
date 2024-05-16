package com.unity.goods.domain.member.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.unity.goods.domain.member.dto.PointBalanceDto.PointBalanceResponse;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

  @InjectMocks
  private PointService pointService;

  @Mock
  private MemberRepository memberRepository;

  @Test
  @DisplayName("잔액 조회 성공 테스트")
  public void getBalanceTest() {
    // given
    Member member = Member.builder()
        .id(1L)
        .balance(1000L)
        .email("test@email.com")
        .build();

    UserDetailsImpl user = new UserDetailsImpl(member);

    given(memberRepository.findByEmail(any(String.class))).willReturn(Optional.of(member));

    // when
    PointBalanceResponse balance = pointService.getBalance(user);

    // then
    assertEquals("1000", balance.getPrice());
  }

  @Test
  @DisplayName("잔액 조회 실패 테스트")
  public void getBalanceFailTest() {
    // given
    Member member = Member.builder()
        .id(1L)
        .balance(1001L)
        .email("test@email.com")
        .build();

    UserDetailsImpl user = new UserDetailsImpl(member);

    given(memberRepository.findByEmail(any(String.class))).willReturn(Optional.of(member));

    // when
    PointBalanceResponse balance = pointService.getBalance(user);

    // then
    assertNotEquals("1000", balance.getPrice());
  }

}