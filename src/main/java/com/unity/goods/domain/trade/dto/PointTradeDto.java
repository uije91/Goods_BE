package com.unity.goods.domain.trade.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

public class PointTradeDto {

  @Getter
  @Builder
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class PointTradeRequest {

    private Long sellerId;

    private Long goodsId;

    @NotBlank(message = "상품 가격을 입력해주세요.")
    @Pattern(regexp = "^[1-9][0-9]*$", message = "가격은 0으로 시작하지 않는 숫자로 입력해야 합니다.")
    private String price;

    @Pattern(regexp = "^[0-9]{6}$", message = "거래 비밀번호는 6자리 숫자로 작성해주세요.")
    private String tradePassword;

  }

  @Getter
  @Builder
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class PointTradeResponse {

    private String paymentStatus;
    private String tradePoint;
    private String remainPoint;

  }

}
