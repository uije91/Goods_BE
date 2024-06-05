package com.unity.goods.domain.goods.exception;

import com.unity.goods.global.exception.CustomException;
import com.unity.goods.global.exception.ErrorCode;

public class GoodsException extends CustomException {

  public GoodsException(ErrorCode errorCode) {
    super(errorCode);
  }
}
