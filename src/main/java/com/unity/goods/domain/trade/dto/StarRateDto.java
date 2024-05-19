package com.unity.goods.domain.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StarRateDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StarRateRequest {
    private double star;
  }

}
