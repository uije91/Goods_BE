package com.unity.goods.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtil {

  public static Cookie addCookie(String key, String value, int maxAge) {
    Cookie cookie = new Cookie(key, value);
    cookie.setHttpOnly(true);
    //cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);

    return cookie;
  }

  public static Cookie deleteCookie(String key, String value) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    return cookie;
  }

  public static String getCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (name.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
