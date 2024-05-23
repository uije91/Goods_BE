package com.unity.goods.domain.oauth.repository;

import com.unity.goods.global.util.CookieUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthorizationRequestRepository implements
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE = "oauth2_auth_request";
  public final static String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
  public final static String REFRESH_TOKEN = "refresh";
  private final static int COOKIE_EXPIRE_SECONDS = 180;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return CookieUtil.getCookies(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE)
        .map(cookie -> CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class))
        .orElse(null);
  }

  //OAuth2AuthorizationRequestRedirectFilter 에서 사용
  @Override
  public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request, HttpServletResponse response) {

    if (authorizationRequest == null) {
      CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE);
      CookieUtil.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
      CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
      return;
    }

    CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE,
        CookieUtil.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);

    String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
    if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
      CookieUtil.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin,
          COOKIE_EXPIRE_SECONDS);
    }
  }

  //OAuth2LoginAuthenticationFilter 에서 사용
  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
      HttpServletResponse response) {
    return this.loadAuthorizationRequest(request);
  }

  //OAuth2AuthenticationSuccessHandler 에서 사용
  public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response){
    CookieUtil.deleteCookie(request,response,OAUTH2_AUTHORIZATION_REQUEST_COOKIE);
    CookieUtil.deleteCookie(request,response,REDIRECT_URI_PARAM_COOKIE_NAME);
    CookieUtil.deleteCookie(request,response,REFRESH_TOKEN);

  }
}
