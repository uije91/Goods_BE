package com.unity.goods.domain.chat.exception;

import com.unity.goods.global.exception.CustomException;
import com.unity.goods.global.exception.ErrorCode;

public class ChatException extends CustomException {

  public ChatException(ErrorCode errorCode) {
    super(errorCode);
  }
}
