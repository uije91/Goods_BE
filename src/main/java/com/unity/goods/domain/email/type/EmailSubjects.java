package com.unity.goods.domain.email.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailSubjects {

  SEND_VERIFICATION_CODE("[인증 번호 발송] : 인증 번호를 확인해주세요");

  private final String title;

}
