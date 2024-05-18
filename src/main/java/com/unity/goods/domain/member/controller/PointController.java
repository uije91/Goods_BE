package com.unity.goods.domain.member.controller;

import com.unity.goods.domain.member.dto.PointBalanceDto;
import com.unity.goods.domain.member.service.PointService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/point")
public class PointController {

  private final PointService pointService;

  @GetMapping("/balance")
  public ResponseEntity<?> getBalance(@AuthenticationPrincipal UserDetailsImpl member) {
    PointBalanceDto.PointBalanceResponse pointBalanceResponse = pointService.getBalance(member);
    return ResponseEntity.ok(pointBalanceResponse);
  }

}
