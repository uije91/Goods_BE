package com.unity.goods.domain.member.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PointWithDrawDto {


  @Getter
  public static  class PointWithDrawRequest {
    @NotBlank(message = "포인트 충전 금액을 입력해주세요.")
    @Pattern(regexp = "^[1-9][0-9]*$", message = "가격은 0으로 시작하지 않는 숫자로 입력해야 합니다.")
    private String price;
  }


  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class PointWithDrawResponse {
    private String remainPoint;
  }

}
