package com.unity.goods.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // System Error
  INTERNAL_SERVER_ERROR(500, "내부 서버 오류가 발생했습니다."),
  BAD_REQUEST_VALID_ERROR(400, "유효성 검사에 실패했습니다."),

  // Email Error
  EMAIL_SEND_ERROR(500, "이메일 전송 과정 중 에러가 발생하였습니다."),
  EMAIL_VERIFICATION_NOT_EXISTS(400, "해당 이메일에 대한 인증 정보가 존재하지 않습니다."),
  INCORRECT_VERIFICATION_NUM(400, "인증 번호가 올바르지 않습니다.");

  private final int status;
  private final String message;
}
