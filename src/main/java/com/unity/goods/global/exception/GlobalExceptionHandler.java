package com.unity.goods.global.exception;

import static com.unity.goods.global.exception.ErrorCode.BAD_REQUEST_VALID_ERROR;
import static com.unity.goods.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;

import com.unity.goods.domain.email.exception.EmailException;
import com.unity.goods.domain.goods.exception.GoodsException;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.oauth.exception.OAuthException;
import com.unity.goods.domain.trade.exception.TradeException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(TradeException.class)
  public ErrorResponse handleTradeException(TradeException e) {
    log.error("Exception \"{}({})\" is occurred.", e.getErrorCode(), e.getErrorCode().getMessage());

    return new ErrorResponse(e.getErrorCode(), e.getErrorCode().getStatus(),
        e.getErrorCode().getMessage());
  }

  @ExceptionHandler(GoodsException.class)
  public ErrorResponse handleGoodsException(GoodsException e) {
    log.error("Exception \"{}({})\" is occurred.", e.getErrorCode(), e.getErrorCode().getMessage());

    return new ErrorResponse(e.getErrorCode(), e.getErrorCode().getStatus(),
        e.getErrorCode().getMessage());
  }

  @ExceptionHandler(OAuthException.class)
  public ErrorResponse handleOAuthException(OAuthException e) {
    log.error("Exception \"{}({})\" is occurred.", e.getErrorCode(), e.getErrorCode().getMessage());

    return new ErrorResponse(e.getErrorCode(), e.getErrorCode().getStatus(),
        e.getErrorCode().getMessage());
  }

  @ExceptionHandler(EmailException.class)
  public ErrorResponse handleEmailException(EmailException e) {
    log.error("Exception \"{}({})\" is occurred.", e.getErrorCode(), e.getErrorCode().getMessage());

    return new ErrorResponse(e.getErrorCode(), e.getErrorCode().getStatus(),
        e.getErrorCode().getMessage());
  }

  @ExceptionHandler(MemberException.class)
  public ErrorResponse handleMemberException(MemberException e) {
    log.error("Exception \"{}({})\" is occurred.", e.getErrorCode(), e.getErrorCode().getMessage());

    return new ErrorResponse(e.getErrorCode(), e.getErrorCode().getStatus(),
        e.getErrorCode().getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    List<String> errors = e.getBindingResult().getAllErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .toList();
    log.error("Exception \"{}({})\" is occurred.", BAD_REQUEST_VALID_ERROR, errors.get(0));
    return new ErrorResponse(
        BAD_REQUEST_VALID_ERROR, 400, errors.get(0));

  }

  @ExceptionHandler(Exception.class)
  public ErrorResponse handleException(Exception e) {
    log.error("Exception {} is occurred.", e.getMessage());

    return new ErrorResponse(
        INTERNAL_SERVER_ERROR, 500, INTERNAL_SERVER_ERROR.getMessage());
  }

}
