package com.unity.goods.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PointBalanceDto {

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  public static class PointBalanceResponse {
    private String price;
  }

}
