package com.unity.goods.domain.email.service;

import static com.unity.goods.global.exception.ErrorCode.EMAIL_SEND_ERROR;
import static com.unity.goods.global.exception.ErrorCode.EMAIL_VERIFICATION_NOT_EXISTS;
import static com.unity.goods.global.exception.ErrorCode.INCORRECT_VERIFICATION_NUM;

import com.unity.goods.domain.email.dto.EmailVerificationCheckDto.EmailVerificationCheckRequest;
import com.unity.goods.domain.email.dto.EmailVerificationCheckDto.EmailVerificationCheckResponse;
import com.unity.goods.domain.email.dto.EmailVerificationDto.EmailVerificationRequest;
import com.unity.goods.domain.email.exception.EmailException;
import com.unity.goods.domain.email.type.EmailSubjects;
import com.unity.goods.infra.service.RedisService;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

  private final static Long VERIFICATION_EXPIRED_AT = 60 * 3 * 1000L; // 3분
  private final static String FROM = "Goods";

  private final RedisService redisService;
  private final MailSender mailSender;

  public void sendVerificationEmail(
      EmailVerificationRequest emailVerificationRequest) {

    String verificationNumber = createVerificationNumber();

    SimpleMailMessage verificationEmail = createVerificationEmail(
        emailVerificationRequest.getEmail(), verificationNumber);

    try {
      mailSender.send(verificationEmail);
      redisService.setDataExpire(emailVerificationRequest.getEmail(), verificationNumber,
          VERIFICATION_EXPIRED_AT);
      log.info("[EmailService] : {}에게 이메일 전송 완료. 인증 번호 redis 저장",
          emailVerificationRequest.getEmail());

    } catch (RuntimeException e) {
      log.error("[EmailService] : 이메일 전송 과정 중 에러 발생");
      throw new EmailException(EMAIL_SEND_ERROR);

    }

  }

  public String createVerificationNumber() {

    SecureRandom random = new SecureRandom();
    int randomNumber = random.nextInt(900000) + 100000;
    log.info("[EmailService] : 인증 번호 생성 완료");

    return String.valueOf(randomNumber);
  }

  public SimpleMailMessage createVerificationEmail(String emailAddress, String verificationNumber) {

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(FROM);
    message.setTo(emailAddress);
    message.setSubject(EmailSubjects.SEND_VERIFICATION_CODE.getTitle());
    message.setText(
        "안녕하세요. 중고거래 마켓 " + FROM + "입니다.\n\n"
            + "인증 번호는 [" + verificationNumber + "] 입니다.\n\n"
            + "인증 번호를 입력하고 인증 완료 버튼을 눌러주세요.");

    log.info("[EmailService] : 인증 이메일 생성 완료. 수신인 : {}", emailAddress);
    return message;
  }

  public EmailVerificationCheckResponse checkIsVerified(EmailVerificationCheckRequest checkRequest) {

    // redis에 존재하는지, 조회되는지
    if (!redisService.existData(checkRequest.getEmail())) {
      throw new EmailException(EMAIL_VERIFICATION_NOT_EXISTS);
    }

    String redisCode = redisService.getData(checkRequest.getEmail());

    // 입력한 값과 redis에 저장된 값이 같은지
    if (!checkRequest.getVerificationNumber().equals(redisCode)) {
      throw new EmailException(INCORRECT_VERIFICATION_NUM);
    }
    log.info("[EmailService] : {} 이메일 인증 확인 ", checkRequest.getEmail());

    return EmailVerificationCheckResponse.builder()
        .verifiedEmail(checkRequest.getEmail())
        .isVerified(true)
        .build();
  }
}
