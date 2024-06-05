package com.unity.goods.domain.member.exception;

import com.unity.goods.global.exception.CustomException;
import com.unity.goods.global.exception.ErrorCode;

public class EmailException extends CustomException {

  public EmailException(ErrorCode errorCode) {
    super(errorCode);
  }
}
