package com.unity.goods.domain.member.controller;

import com.unity.goods.domain.member.dto.LoginDto;
import com.unity.goods.domain.member.dto.SignUpDto;
import com.unity.goods.domain.member.service.MemberService;
import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.global.util.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

  private final MemberService memberService;
  private final int COOKIE_EXPIRATION = 30 * 24 * 60 * 60; // 30일

  @PostMapping("/signup")
  public ResponseEntity<?> signUp(
      @RequestBody @Valid SignUpDto.SignUpRequest signUpRequest) {
    SignUpDto.SignUpResponse signUpResponse = memberService.signUp(signUpRequest);
    return ResponseEntity.ok(signUpResponse);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginDto.LoginRequest request) {
    TokenDto login = memberService.login(request);
    CookieUtil.addCookie("refresh-token", login.getRefreshToken(), COOKIE_EXPIRATION);
    return ResponseEntity.ok()
        //RFC 7235 정의에 따라 인증헤더 형태를 가져야 한다.
        .header(HttpHeaders.AUTHORIZATION,"Bearer "+login.getAccessToken())
        .body(login);
  }
}
