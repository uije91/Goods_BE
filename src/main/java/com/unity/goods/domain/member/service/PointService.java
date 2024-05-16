package com.unity.goods.domain.member.service;

import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.member.dto.PointBalanceDto.PointBalanceResponse;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.global.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

  private final MemberRepository memberRepository;

  public PointBalanceResponse getBalance(UserDetailsImpl member) {
    // 인증된 사용자 정보가 db에서 존재하는지 검사
    Member authenticatedUser = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    return PointBalanceResponse.builder().price(String.valueOf(authenticatedUser.getBalance()))
        .build();
  }
}
