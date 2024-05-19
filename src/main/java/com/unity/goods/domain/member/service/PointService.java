package com.unity.goods.domain.member.service;

import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.member.dto.PointBalanceDto.PointBalanceResponse;
import com.unity.goods.domain.member.dto.PointChargeDto.PointChargeRequest;
import com.unity.goods.domain.member.dto.PointChargeDto.PointChargeResponse;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.PaymentStatus;
import com.unity.goods.global.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

  private final MemberRepository memberRepository;

  @Transactional
  public PointChargeResponse chargePoint(UserDetailsImpl member,
      PointChargeRequest pointChargeRequest) {

    Member authenticatedUser = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    // TODO 포트원 결제내역 단건 조회 API 호출

    // 충전 포인트 저장
    authenticatedUser.setBalance(Long.valueOf(pointChargeRequest.getPrice()));

    return PointChargeResponse.builder()
        .paymentStatus(PaymentStatus.SUCCESS)
        .build();
  }

  public PointBalanceResponse getBalance(UserDetailsImpl member) {
    // 인증된 사용자 정보가 db에서 존재하는지 검사
    Member authenticatedUser = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    return PointBalanceResponse.builder().price(String.valueOf(authenticatedUser.getBalance()))
        .build();
  }
}
