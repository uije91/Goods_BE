package com.unity.goods.domain.member.exception;

import com.unity.goods.global.exception.CustomException;
import com.unity.goods.global.exception.ErrorCode;

public class MemberException extends CustomException {

  public MemberException(ErrorCode errorCode) {
    super(errorCode);
  }
}
