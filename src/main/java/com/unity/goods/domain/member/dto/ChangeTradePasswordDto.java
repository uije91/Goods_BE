package com.unity.goods.domain.member.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChangeTradePasswordDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class ChangeTradePasswordRequest {

    @Pattern(regexp = "^[0-9]{6}$", message = "거래 비밀번호는 6자리 숫자로 작성해주세요.")
    private String curTradePassword;

    @Pattern(regexp = "^[0-9]{6}$", message = "거래 비밀번호는 6자리 숫자로 작성해주세요.")
    private String newTradePassword;

  }
}
