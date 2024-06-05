package com.unity.goods.domain.member.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.unity.goods.domain.member.type.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

public class PointChargeDto {

  @Getter
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class PointChargeRequest {

    @NotBlank(message = "포인트 충전 금액을 입력해주세요.")
    @Pattern(regexp = "^[1-9][0-9]*$", message = "가격은 0으로 시작하지 않는 숫자로 입력해야 합니다.")
    private String price;

    @NotBlank(message = "주문 번호는 필수입니다.")
    private String paymentId;
  }

  @Getter
  @Builder
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class PointChargeResponse {
    private String paymentStatus;
  }

}
