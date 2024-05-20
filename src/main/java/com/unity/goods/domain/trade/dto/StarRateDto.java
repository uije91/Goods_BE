package com.unity.goods.domain.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StarRateDto {

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StarRateRequest {
    private double star;
  }

}
