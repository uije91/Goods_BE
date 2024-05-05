package com.unity.goods.domain.oauth.exception;

import com.unity.goods.global.exception.CustomException;
import com.unity.goods.global.exception.ErrorCode;

public class OAuthException extends CustomException {

  public OAuthException(ErrorCode errorCode) {
    super(errorCode);
  }
}
