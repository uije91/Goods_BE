package com.unity.goods.domain.goods.controller;

import com.unity.goods.domain.goods.dto.GoodsDetailDto;
import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoRequest;
import com.unity.goods.domain.goods.dto.UpdateGoodsInfoDto.UpdateGoodsInfoResponse;
import com.unity.goods.domain.goods.dto.UpdateGoodsStateDto.UpdateGoodsStateRequest;
import com.unity.goods.domain.goods.dto.UploadGoodsDto;
import com.unity.goods.domain.goods.service.GoodsService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goods")
public class GoodsController {

  private final GoodsService goodsService;

  @PostMapping("/new")
  public ResponseEntity<?> uploadGoods(
      @AuthenticationPrincipal UserDetailsImpl member,
      @Valid @ModelAttribute UploadGoodsDto.UploadGoodsRequest uploadGoodsRequest
  ) {
    UploadGoodsDto.UploadGoodsResponse uploadGoodsResponse = goodsService.uploadGoods(member,
        uploadGoodsRequest);
    return ResponseEntity.ok(uploadGoodsResponse);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getDetailGoods(
      @AuthenticationPrincipal UserDetailsImpl member,
      @PathVariable Long id
  ) {
    GoodsDetailDto.GoodsDetailResponse goodsDetailResponse =
        goodsService.getDetailGoods(member, id);
    return ResponseEntity.ok(goodsDetailResponse);
  }

  @PutMapping("/{goodsId}")
  public ResponseEntity<?> updateGoodsInfo(
      @AuthenticationPrincipal UserDetailsImpl member,
      @PathVariable Long goodsId,
      @Valid @ModelAttribute UpdateGoodsInfoRequest updateGoodsInfoRequest) {

    UpdateGoodsInfoResponse updateGoodsInfoResponse = goodsService.updateGoodsInfo(goodsId, member,
        updateGoodsInfoRequest);

    return ResponseEntity.ok(updateGoodsInfoResponse);
  }

  @PutMapping("/{goodsId}/state")
  public ResponseEntity<?> updateState(
      @AuthenticationPrincipal UserDetailsImpl member,
      @PathVariable Long goodsId,
      @RequestBody UpdateGoodsStateRequest updateGoodsStateRequest) {
    goodsService.updateState(member, goodsId, updateGoodsStateRequest);
    return ResponseEntity.ok().build();
  }

}
