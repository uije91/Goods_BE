package com.unity.goods.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // Member Error
  USER_NOT_FOUND(404,"회원 정보를 찾을 수 없습니다"),
  USE_SOCIAL_LOGIN(400, "소셜 로그인을 이용해주세요"),
  PASSWORD_NOT_MATCH(400, "비밀번호가 올바르지 않습니다."),
  EMAIL_NOT_VERITY(400, "이메일 인증이 완료되지 않았습니다."),
  RESIGN_ACCOUNT(400, "탈퇴한 이메일입니다."),

  // System Error
  INTERNAL_SERVER_ERROR(500, "내부 서버 오류가 발생했습니다."),
  BAD_REQUEST_VALID_ERROR(400, "유효성 검사에 실패했습니다.");

  private final int status;
  private final String message;
}
