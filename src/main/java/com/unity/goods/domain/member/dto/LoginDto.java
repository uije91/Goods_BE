package com.unity.goods.domain.member.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class LoginDto {

  @Getter
  @AllArgsConstructor
  @Builder
  public static class LoginRequest {

    private String email;
    private String password;
  }

  @Getter
  @AllArgsConstructor
  @Builder
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class LoginResponse {
    private String accessToken;
  }
}
