package com.unity.goods.domain.trade.exception;

import com.unity.goods.global.exception.CustomException;
import com.unity.goods.global.exception.ErrorCode;

public class TradeException extends CustomException {
  public TradeException(ErrorCode errorCode) {
    super(errorCode);
  }
}
