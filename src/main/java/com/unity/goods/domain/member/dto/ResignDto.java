package com.unity.goods.domain.member.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ResignDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ResignRequest{
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,20}$",
        message = "비밀번호는 8~20자 영문,숫자,특수문자를 사용하세요.")
    private String password;
  }

}
