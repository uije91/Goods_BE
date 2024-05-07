package com.unity.goods.domain.member.controller;

import static com.unity.goods.domain.member.dto.FindPasswordDto.FindPasswordRequest;

import com.unity.goods.domain.member.dto.LoginDto;
import com.unity.goods.domain.member.dto.ResignDto;
import com.unity.goods.domain.member.dto.SignUpDto;
import com.unity.goods.domain.member.service.MemberService;
import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
      @Valid @ModelAttribute SignUpDto.SignUpRequest signUpRequest) {
    SignUpDto.SignUpResponse signUpResponse = memberService.signUp(signUpRequest);
    return ResponseEntity.ok(signUpResponse);
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginDto.LoginRequest request) {
    TokenDto login = memberService.login(request);
    Cookie cookie = CookieUtil.addCookie("refresh", login.getRefreshToken(),
        COOKIE_EXPIRATION);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.getName() + "=" + cookie.getValue())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.getAccessToken())
        .build();
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader("Authorization") String requestAccessToken) {
    memberService.logout(requestAccessToken);
    Cookie cookie = CookieUtil.deleteCookie("refresh", null);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
  }

  @PutMapping("/resign")
  public ResponseEntity<?> resign(
      @RequestHeader("Authorization") String accessToken,
      @AuthenticationPrincipal UserDetailsImpl member,
      @RequestBody ResignDto.ResignRequest resignRequest
  ) {
    memberService.resign(accessToken, member, resignRequest);
    CookieUtil.deleteCookie("refresh", null);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/find")
  public ResponseEntity<?> findPassword(
      @RequestBody @Valid FindPasswordRequest findPasswordRequest) {
    memberService.findPassword(findPasswordRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reissue")
  public ResponseEntity<?> reissue(HttpServletRequest request) {
    TokenDto tokenDto = TokenDto.builder()
        .accessToken(request.getHeader(HttpHeaders.AUTHORIZATION))
        .refreshToken(CookieUtil.getCookie(request, "refresh"))
        .build();

    String accessToken = memberService.reissue(tokenDto);

    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).build();
  }
}
