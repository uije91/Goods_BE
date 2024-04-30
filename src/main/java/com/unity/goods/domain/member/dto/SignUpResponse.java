package com.unity.goods.domain.member.dto;

import lombok.Getter;

@Getter
public class SignUpResponse {

  private String profileImageUrl;
  private String email;
  private String nickName;
  private String phoneNumber;

}
