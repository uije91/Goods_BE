package com.unity.goods.domain.goods.controller;

import com.unity.goods.domain.goods.dto.UploadGoodsDto;
import com.unity.goods.domain.goods.service.GoodsService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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

}
