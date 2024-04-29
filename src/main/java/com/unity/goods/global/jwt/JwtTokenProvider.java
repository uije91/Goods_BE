package com.unity.goods.global.jwt;

import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.global.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  private final UserDetailsServiceImpl userDetailsService;
  private final RedisService redisService;

  @Value("${spring.jwt.access-token-expiration-time}")
  private long ACCESS_TOKEN_EXPIRE_TIME;

  @Value("${spring.jwt.refresh-token-expiration-time}")
  private long REFRESH_TOKEN_EXPIRE_TIME;

  private final Key key;

  // 시크릿 키 설정
  public JwtTokenProvider(@Value("${spring.jwt.secret}") String secret,
      UserDetailsServiceImpl userDetailsService, RedisService redisService) {
    this.userDetailsService = userDetailsService;
    this.redisService = redisService;
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    key = Keys.hmacShaKeyFor(keyBytes);
  }


  // AT, RT 생성
  public TokenDto generateToken(String email, String role) {
    Date now = new Date();

    Date accessTokenExpiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);
    String accessToken = Jwts.builder()
        .setExpiration(accessTokenExpiration)
        .setSubject("access-token")
        .claim("email", email)
        .claim("role", role)
        .signWith(key, SignatureAlgorithm.HS512)
        .compact();

    Date refreshTokenExpiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);
    String refreshToken = Jwts.builder()
        .setExpiration(refreshTokenExpiration)
        .setSubject("refresh-token")
        .signWith(key, SignatureAlgorithm.HS512)
        .compact();

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  // 토큰으로부터 정보 추출
  public Claims getClaims(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
  public Authentication getAuthentication(String token) {
    String email = getClaims(token).get("email").toString();

    UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  /**
   * 토큰 검증
   */
  public boolean validateRefreshToken(String refreshToken) {
    try {
      if (redisService.getData(refreshToken).equals("delete")) { // 회원탈퇴 했을 경우
        return false;
      }
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.info("Invalid JWT Token", e);
    } catch (ExpiredJwtException e) {
      log.info("Expired JWT Token", e);
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported JWT Token", e);
    } catch (IllegalArgumentException e) {
      log.info("JWT claims string is empty.", e);
    }
    return false;
  }

  // Filter 에서 사용
  public boolean validateAccessToken(String accessToken) {
    try {
      if (redisService.getData(accessToken) != null // NPE 방지
          && redisService.getData(accessToken).equals("logout")) { // 로그아웃 했을 경우
        return false;
      }
      Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(accessToken);
      return true;
    } catch (ExpiredJwtException e) {
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // 토큰 만료 여부 확인 ( 토큰 재발급시 사용 )
  public boolean isExpired(String token) {
    try {
      return getClaims(token).getExpiration().before(new Date());
    } catch (ExpiredJwtException e) { // 토큰 만료
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
