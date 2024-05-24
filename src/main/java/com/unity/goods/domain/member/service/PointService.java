package com.unity.goods.domain.member.service;

import static com.unity.goods.global.exception.ErrorCode.PAYMENT_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.PAYMENT_UNMATCHED;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.Payment;
import com.unity.goods.domain.member.dto.PointBalanceDto.PointBalanceResponse;
import com.unity.goods.domain.member.dto.PointChargeDto.PointChargeRequest;
import com.unity.goods.domain.member.dto.PointChargeDto.PointChargeResponse;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.PaymentStatus;
import com.unity.goods.domain.trade.exception.TradeException;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

  private final MemberRepository memberRepository;
//  private final IamportClient iamportClient;

  @Transactional
  public PointChargeResponse chargePoint(UserDetailsImpl member,
      PointChargeRequest pointChargeRequest) {

    Member authenticatedUser = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    // 포트원 결제내역 단건 조회 API 호출
//    Payment payment = null;
//    try {
//      payment = iamportClient.paymentByImpUid(pointChargeRequest.getImpUid()).getResponse();
//    } catch (IamportResponseException | IOException e) {
//      throw new TradeException(PAYMENT_NOT_FOUND);
//    }

    // 조회된 결제 내역과 요청 충전 금액이 같은지 확인
//    if (!Objects.equals(payment.getAmount(),
//        BigDecimal.valueOf(Long.parseLong(pointChargeRequest.getPrice())))) {
//      throw new TradeException(PAYMENT_UNMATCHED);
//    }

    // 충전 포인트 저장
    authenticatedUser.setBalance(Long.valueOf(pointChargeRequest.getPrice()));

    return PointChargeResponse.builder()
        .paymentStatus(PaymentStatus.SUCCESS.getDescription())
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
