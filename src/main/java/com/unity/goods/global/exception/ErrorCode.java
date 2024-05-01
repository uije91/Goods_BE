package com.unity.goods.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // System Error
  INTERNAL_SERVER_ERROR(500, "내부 서버 오류가 발생했습니다."),
  BAD_REQUEST_VALID_ERROR(400, "유효성 검사에 실패했습니다."),

  // Member
  MEMBER_NOT_FOUND(404, "멤버를 찾을 수 없습니다."),
  CURRENT_USED_PASSWORD(404, "현재 사용중인 비밀번호입니다.");

  private final int status;
  private final String message;
}
