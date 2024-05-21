package com.unity.goods.domain.oauth.handler;

import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.global.jwt.JwtTokenProvider;
import com.unity.goods.global.util.CookieUtil;
import com.unity.goods.infra.service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final RedisService redisService;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {

    // JWT 생성을 위해 email 과 Role 값을 받기
    String email = authentication.getName();

    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
    GrantedAuthority auth = iterator.next();
    String role = auth.getAuthority();

    // AT, RT 생성 및 Redis 에 RT 저장
    TokenDto tokenDto = jwtTokenProvider.generateToken(email, role);

    // Redis 에 RT가 존재할 경우 -> 삭제
    if (redisService.getData("RT:" + email) != null) {
      redisService.deleteData("RT:" + email);
    }

    // Redis 에  RefreshToken 저장
    long expiration =
        jwtTokenProvider.getTokenExpirationTime(tokenDto.getRefreshToken()) - new Date().getTime();

    redisService.setDataExpire("RT:" + email, tokenDto.getRefreshToken(), expiration);

    Cookie token =
        CookieUtil.addCookie("refresh", tokenDto.getRefreshToken(), 30 * 24 * 60 * 60);

    response.addCookie(token);
    response.addHeader(HttpHeaders.SET_COOKIE, token.getName() + "=" + token.getValue());

    String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/auth/kakao")
        .queryParam("access", tokenDto.getAccessToken())
        .build().toUriString();
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}



