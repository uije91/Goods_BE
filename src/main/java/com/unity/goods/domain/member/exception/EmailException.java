package com.unity.goods.domain.member.exception;

public class EmailException extends RuntimeException{

  public String message;

  public EmailException(String message) {
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }
}
