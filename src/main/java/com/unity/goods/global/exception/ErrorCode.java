package com.unity.goods.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // OAuth Error
  INVALID_SOCIAL_TYPE(400, "잘못된 소셜 타입입니다"),
  USE_SERVER_LOGIN(400, "기존 회원입니다. 자체 로그인을 이용해주세요."),

  // Member Error
  USER_NOT_FOUND(404, "회원 정보를 찾을 수 없습니다"),
  USE_SOCIAL_LOGIN(400, "소셜 로그인을 이용해주세요"),
  PASSWORD_NOT_MATCH(400, "비밀번호가 올바르지 않습니다."),
  EMAIL_NOT_VERITY(400, "이메일 인증이 완료되지 않았습니다."),
  RESIGNED_ACCOUNT(400, "탈퇴한 이메일입니다."),
  CURRENT_USED_PASSWORD(404, "현재 사용중인 비밀번호입니다."),
  ALREADY_REGISTERED_USER(400, "이미 가입한 회원입니다."),
  NICKNAME_ALREADY_EXISTS(400, "동일한 닉네임이 존재합니다."),
  INVALID_REFRESH_TOKEN(400, "잘못된 리프레시 토큰입니다."),

  // Goods Error
  GOODS_NOT_FOUND(400, "조회되지 않는 상품입니다."),
  MISMATCHED_SELLER(404, "판매자 정보가 일치하지 않습니다."),
  ALREADY_SOLD_OUT_GOODS(400, "이미 판매가 완료된 상품입니다."),
  MAX_IMAGE_LIMIT_EXCEEDED(404, "등록할 수 있는 이미지의 최대 개수를 초과하였습니다."),

  // System Error
  INTERNAL_SERVER_ERROR(500, "내부 서버 오류가 발생했습니다."),
  BAD_REQUEST_VALID_ERROR(400, "유효성 검사에 실패했습니다."),

  // Email Error
  EMAIL_SEND_ERROR(500, "이메일 전송 과정 중 에러가 발생하였습니다."),
  EMAIL_VERIFICATION_NOT_EXISTS(400, "해당 이메일에 대한 인증 정보가 존재하지 않습니다."),
  INCORRECT_VERIFICATION_NUM(400, "인증 번호가 올바르지 않습니다."),

  // JwtFilterAuthenticationError
  UNAUTHORIZED(401, "인증되지 않은 사용자입니다.");

  private final int status;
  private final String message;
}
