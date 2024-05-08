package com.unity.goods.global.exception;

public class JwtFilterAuthenticationException extends CustomException{

  public JwtFilterAuthenticationException(ErrorCode errorCode) {
    super(errorCode);
  }
}
