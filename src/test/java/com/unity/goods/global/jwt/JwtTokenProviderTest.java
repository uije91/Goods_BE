package com.unity.goods.global.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unity.goods.domain.model.TokenDto;
import io.jsonwebtoken.Claims;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JwtTokenProviderTest {

  @Autowired
  JwtTokenProvider jwtTokenProvider;

  @Test
  @DisplayName("시크릿 키 설정 테스트")
  public void secretKey_test() {
    String secret = "c2VjcmV0c2VjcmV0c2VjcmV0c2VjcmV0c2VjcmV0c2VjcmV0";

    byte[] keyBytes = Base64.getDecoder().decode(secret);
    SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);

    assertNotNull(key);
    assertEquals("HmacSHA256", key.getAlgorithm());
  }

  @Test
  @DisplayName("토큰 생성 테스트")
  void generateToken_test() {
    TokenDto tokenDto = jwtTokenProvider.generateToken("a@a.com", "USER");
    long REFRESH_TOKEN_EXPIRE_TIME = 14 * 24 * 60 * 60 * 1000; // 14일

    assertNotNull(tokenDto);
    assertNotNull(tokenDto.getAccessToken());
    assertNotNull(tokenDto.getRefreshToken());

    Claims refreshTokenClaims = jwtTokenProvider.getClaims(tokenDto.getRefreshToken());
    Date refreshTokenExpiration = refreshTokenClaims.getExpiration();
    assertNotNull(refreshTokenExpiration);
    assertTrue(refreshTokenExpiration.after(new Date()));
  }

  @Test
  @DisplayName("토큰 정보 추출 테스트")
  void getClaims_test() {
    TokenDto tokenDto = jwtTokenProvider.generateToken("a@a.com", "USER");

    Claims accessTokenClaims = jwtTokenProvider.getClaims(tokenDto.getAccessToken());
    assertEquals("access-token", accessTokenClaims.getSubject());
    assertEquals("a@a.com", accessTokenClaims.get("email"));
    assertEquals("USER", accessTokenClaims.get("role"));

    Claims refreshTokenClaims = jwtTokenProvider.getClaims(tokenDto.getRefreshToken());
    assertEquals("refresh-token", refreshTokenClaims.getSubject());
  }


}
