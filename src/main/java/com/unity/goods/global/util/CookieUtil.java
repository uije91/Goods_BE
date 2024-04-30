package com.unity.goods.global.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

  public static Cookie addCookie(String key, String value, int maxAge) {
    Cookie cookie = new Cookie(key, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setMaxAge(maxAge);

    return cookie;
  }
}
