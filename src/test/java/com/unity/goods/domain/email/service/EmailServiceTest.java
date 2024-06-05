package com.unity.goods.domain.email.service;

import static com.unity.goods.global.exception.ErrorCode.INCORRECT_VERIFICATION_NUM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import com.unity.goods.domain.member.dto.EmailVerificationCheckDto.EmailVerificationCheckRequest;
import com.unity.goods.domain.member.exception.EmailException;
import com.unity.goods.domain.member.type.EmailSubjects;
import com.unity.goods.domain.member.service.EmailService;
import com.unity.goods.infra.service.RedisService;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @InjectMocks
  private EmailService emailService;

  @Mock
  private RedisService redisService;

  @Test
  @DisplayName("인증 번호 6자리(100000 ~ 999999) 생성")
  public void createVerificationNumberTest() {
    String verificationNumber = emailService.createVerificationNumber();
    int number = Integer.parseInt(verificationNumber);
    assertTrue(number >= 100000 && number <= 999999,
        "Verification number should be between 100000 and 999999.");
  }

  @Test
  @DisplayName("인증 코드 이메일 전송 체크")
  public void testCreateVerificationEmail() {
    // given
    String testEmail = "fortestseowon@gmail.com";
    String testVerificationNumber = "123456";

    // when
    SimpleMailMessage message = emailService.createVerificationEmail(testEmail,
        testVerificationNumber);

    // then
    assertEquals(testEmail, Objects.requireNonNull(message.getTo())[0], "");
    assertEquals(EmailSubjects.SEND_VERIFICATION_CODE.getTitle(), message.getSubject(), "");
    String expectedText = "안녕하세요. 중고거래 마켓 Goods입니다.\n\n" +
        "인증 번호는 [" + testVerificationNumber + "] 입니다.\n\n" +
        "인증 번호를 입력하고 인증 완료 버튼을 눌러주세요.";
    assertEquals(expectedText, message.getText(),
        "The email text should include the verification number and correct message.");

  }

  @Test
  @DisplayName("이메일 인증 확인 성공")
  public void checkIsVerifiedSuccessTest() {
    String testEmail = "fortestseowon@gmail.com";
    String testVerificationNumber = "123456";

    EmailVerificationCheckRequest checkRequest = EmailVerificationCheckRequest.builder()
        .email(testEmail)
        .verificationNumber(testVerificationNumber)
        .build();

    // given
    given(redisService.existData(checkRequest.getEmail())).willReturn(true);
    given(redisService.getData(checkRequest.getEmail())).willReturn(testVerificationNumber);

    // when
    emailService.checkIsVerified(checkRequest);

  }

  @Test
  @DisplayName("이메일 인증 확인 실패")
  public void checkIsVerifiedFailTest() {

    String testEmail = "fortestseowon@gmail.com";
    String testVerificationNumber = "123456";

    EmailVerificationCheckRequest checkRequest = EmailVerificationCheckRequest.builder()
        .email(testEmail)
        .verificationNumber(testVerificationNumber)
        .build();

    // given
    given(redisService.existData(checkRequest.getEmail())).willReturn(true);
    given(redisService.getData(checkRequest.getEmail())).willReturn("X" + testVerificationNumber);

    // when
    EmailException exception =
        assertThrows(EmailException.class, () ->
            emailService.checkIsVerified(checkRequest));

    // then
    assertEquals(INCORRECT_VERIFICATION_NUM, exception.getErrorCode());

  }

}