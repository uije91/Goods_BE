package com.unity.goods.domain.member.controller;

import com.unity.goods.domain.member.dto.PointBalanceDto;
import com.unity.goods.domain.member.dto.PointChargeDto.PointChargeRequest;
import com.unity.goods.domain.member.dto.PointChargeDto.PointChargeResponse;
import com.unity.goods.domain.member.dto.PointWithDrawDto;
import com.unity.goods.domain.member.dto.PointWithDrawDto.PointWithDrawResponse;
import com.unity.goods.domain.member.service.PointService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/point")
public class PointController {

  private final PointService pointService;

  // TODO iamport 단건 조회 구현 필요
  @PostMapping("/charge")
  public ResponseEntity<PointChargeResponse> chargePoint(
      @AuthenticationPrincipal UserDetailsImpl member,
      @Valid @RequestBody PointChargeRequest pointChargeRequest) {
    PointChargeResponse pointChargeResponse = pointService.chargePoint(member, pointChargeRequest);
    return ResponseEntity.ok(pointChargeResponse);
  }

  @GetMapping("/balance")
  public ResponseEntity<?> getBalance(@AuthenticationPrincipal UserDetailsImpl member) {
    PointBalanceDto.PointBalanceResponse pointBalanceResponse = pointService.getBalance(member);
    return ResponseEntity.ok(pointBalanceResponse);
  }

  // TODO iamport 결제 취소 기능 구현
  @PostMapping("/withdraw")
  public ResponseEntity<?> withdraw(
      @AuthenticationPrincipal UserDetailsImpl member,
      @Valid @RequestBody PointWithDrawDto.PointWithDrawRequest pointWithDrawRequest) {
    PointWithDrawResponse pointWithDrawResponse = pointService.withdraw(member, pointWithDrawRequest);
    return ResponseEntity.ok(pointWithDrawResponse);
  }

}
