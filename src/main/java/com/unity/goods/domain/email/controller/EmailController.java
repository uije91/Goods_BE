package com.unity.goods.domain.email.controller;

import com.unity.goods.domain.email.dto.EmailVerificationCheckDto.EmailVerificationCheckRequest;
import com.unity.goods.domain.email.dto.EmailVerificationDto.EmailVerificationRequest;
import com.unity.goods.domain.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

  private final EmailService emailService;

  @PostMapping("/verification")
  public ResponseEntity<?> sendVerificationEmail(
      @RequestBody EmailVerificationRequest emailVerificationRequest) {
    emailService.sendVerificationEmail(emailVerificationRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/verification/check")
  public ResponseEntity<?> checkIsVerified(
      @RequestBody EmailVerificationCheckRequest checkRequest) {
    emailService.checkIsVerified(checkRequest);
    return ResponseEntity.ok().build();
  }

}
