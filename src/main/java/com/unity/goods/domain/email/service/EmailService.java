package com.unity.goods.domain.email.service;

import com.unity.goods.domain.email.dto.EmailVerificationDto.EmailVerificationRequest;
import com.unity.goods.global.service.RedisService;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

  private final static Long VERIFICATION_EXPIRED_AT = 60 * 3 * 1000L; // 3분
  private final RedisService redisService;

  public void sendVerificationEmail(
      EmailVerificationRequest emailVerificationRequest) {

    String verificationNumber = createVerificationNumber();

    redisService.setDataExpire(emailVerificationRequest.getEmail(),
        verificationNumber,
        VERIFICATION_EXPIRED_AT);
    log.info("[sendVerificationEmail] : 생성된 인증 번호 {} ", verificationNumber);
  }

  public String createVerificationNumber() {

    SecureRandom random = new SecureRandom();
    int randomNumber = random.nextInt(900000) + 100000;
    log.info("[createVerificationNumber] : 인증 번호 생성 완료");

    return String.valueOf(randomNumber);
  }

}
