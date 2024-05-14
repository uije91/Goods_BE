package com.unity.goods.domain.trade.controller;

import com.unity.goods.domain.trade.dto.PurchasedListDto.PurchasedListResponse;
import com.unity.goods.domain.trade.service.TradeService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trade")
public class TradeController {

  private final TradeService tradeService;

  @GetMapping("/purchased-list")
  public ResponseEntity<?> purchasedGoodsList(
      @AuthenticationPrincipal UserDetailsImpl member,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Page<PurchasedListResponse> purchasedList
        = tradeService.getPurchasedList(member, page, size);

    return ResponseEntity.ok(purchasedList);
  }
}
