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
  EMAIL_SEND_ERROR(500, "이메일 전송 과정 중 에러가 발생하였습니다.");

  private final int status;
  private final String message;
}
