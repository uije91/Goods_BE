package com.unity.goods.domain.email.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailServiceTest {

  @Autowired
  EmailService emailService;

  @Test
  @DisplayName("인증 번호 6자리 생성")
  public void createVerificationNumberTest() {
    String verificationNumber = emailService.createVerificationNumber();
    int number = Integer.parseInt(verificationNumber);
    assertTrue(number >= 100000 && number <= 999999,
        "Verification number should be between 100000 and 999999.");
  }

}