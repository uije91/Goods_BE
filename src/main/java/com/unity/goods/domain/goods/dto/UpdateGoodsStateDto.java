package com.unity.goods.domain.goods.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UpdateGoodsStateDto {

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class UpdateGoodsStateRequest {

    @NotNull(message = "상품 상태를 입력해주세요.")
    private String goodsStatus;

  }
}
