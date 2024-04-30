package com.unity.goods.domain.member.dto;

import lombok.Getter;

@Getter
public class SignUpRequest {

  // 비밀번호와 비밀번호 확인이 서로 일치하는지 확인

  private String profileImageUrl;
  private String email;
  private String password;
  private String chkPassword;
  private String nickName;
  private String phoneNumber;
  private String tradePassword;

}
