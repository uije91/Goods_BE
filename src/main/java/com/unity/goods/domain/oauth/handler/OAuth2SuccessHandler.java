package com.unity.goods.domain.oauth.handler;

import static com.unity.goods.domain.oauth.repository.CustomAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.domain.oauth.repository.CustomAuthorizationRequestRepository;
import com.unity.goods.global.jwt.JwtTokenProvider;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.global.util.CookieUtil;
import com.unity.goods.infra.service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final RedisService redisService;
  private final JwtTokenProvider jwtTokenProvider;
  private final CustomAuthorizationRequestRepository authorizationRequestRepository;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    String targetUrl = determineTargetUrl(request, response, authentication);
    TokenDto tokenDto = saveUser(authentication);

    CookieUtil.deleteCookie(request, response, "refresh");
    CookieUtil.addCookie(response, "refresh", tokenDto.getRefreshToken(), 2592000);

    getRedirectStrategy().sendRedirect(request, response, getRedirectUrl(targetUrl, tokenDto));
  }

  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    Optional<String> redirectUri = CookieUtil.getCookies(request, REDIRECT_URI_PARAM_COOKIE_NAME)
        .map(Cookie::getValue);
    //이미 OAuth2LoginAuthenticationFilter에서 authentication을 꺼내왔고 위에서 redirectUrl을 받아왔으므로 쿠키의 값은 제거
    clearAuthenticationAttributes(request, response);
    return redirectUri.orElse(getDefaultTargetUrl());
  }

  private TokenDto saveUser(Authentication authentication) {
    // JWT 생성을 위해 email 과 Role 값을 받기
    UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
    String email = user.getName();
    String role = user.getAuthorities().iterator().next().getAuthority();

    log.info("email : {} ", email);
    log.info("role : {}", role);

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

    return tokenDto;
  }


  private String getRedirectUrl(String targetUrl, TokenDto token) {
    return UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("access", token.getAccessToken())
        .build().toUriString();
  }

  // 권한 정보 삭제
  protected void clearAuthenticationAttributes(HttpServletRequest request,
      HttpServletResponse response) {
    super.clearAuthenticationAttributes(request);
    authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
  }
}

