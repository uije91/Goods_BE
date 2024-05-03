package com.unity.goods.global.util;

import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtil {

  public static Cookie addCookie(String key, String value, int maxAge) {
    Cookie cookie = new Cookie(key, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setMaxAge(maxAge);

    return cookie;
  }

  public static Cookie deleteCookie(String key, String value) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    return cookie;
  }
}
