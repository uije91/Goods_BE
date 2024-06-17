package com.unity.goods.domain.notification.controller;

import com.unity.goods.domain.notification.dto.FcmTokenDto;
import com.unity.goods.domain.notification.service.FcmService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

  private final FcmService fcmService;

  @PostMapping("/new")
  public ResponseEntity<?> registerFcmToken(
      @AuthenticationPrincipal UserDetailsImpl member,
      @RequestBody FcmTokenDto fcmTokenDto) {
    fcmService.registerFcmToken(member, fcmTokenDto);
    return ResponseEntity.ok().build();
  }


}
