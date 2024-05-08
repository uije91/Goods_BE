package com.unity.goods.domain.goods.controller;

import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoRequest;
import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoResponse;
import com.unity.goods.domain.goods.service.GoodsService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goods")
public class GoodsController {

  private final GoodsService goodsService;

  @PutMapping("/{goodsId}")
  public ResponseEntity<?> updateGoodsInfo(
      @PathVariable Long goodsId,
      @AuthenticationPrincipal UserDetailsImpl member,
      @RequestBody UpdateGoodsInfoRequest updateGoodsInfoRequest) {

    UpdateGoodsInfoResponse updateGoodsInfoResponse = goodsService.updateGoodsInfo(goodsId, member,
        updateGoodsInfoRequest);

    return ResponseEntity.ok(updateGoodsInfoResponse);
  }


}
