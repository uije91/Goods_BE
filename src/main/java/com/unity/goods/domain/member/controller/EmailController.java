package com.unity.goods.domain.member.controller;

import com.unity.goods.domain.member.dto.EmailVerificationCheckDto.EmailVerificationCheckRequest;
import com.unity.goods.domain.member.dto.EmailVerificationCheckDto.EmailVerificationCheckResponse;
import com.unity.goods.domain.member.dto.EmailVerificationDto.EmailVerificationRequest;
import com.unity.goods.domain.member.service.EmailService;
import jakarta.validation.Valid;
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
      @RequestBody @Valid EmailVerificationRequest emailVerificationRequest) {
    emailService.sendVerificationEmail(emailVerificationRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/verification/check")
  public ResponseEntity<?> checkIsVerified(
      @RequestBody @Valid EmailVerificationCheckRequest checkRequest) {
    EmailVerificationCheckResponse emailVerificationCheckResponse =
        emailService.checkIsVerified(checkRequest);
    return ResponseEntity.ok(emailVerificationCheckResponse);
  }

}
