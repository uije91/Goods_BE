package com.unity.goods.domain.notification.exception;

import com.unity.goods.global.exception.CustomException;
import com.unity.goods.global.exception.ErrorCode;

public class FCMException extends CustomException {
  public FCMException(ErrorCode errorCode) {
    super(errorCode);
  }
}
