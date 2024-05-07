package com.unity.goods.global.jwt;

import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.infra.service.RedisService;
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

  private static final String TOKEN_PREFIX = "Bearer ";
  private final Key key;

  // 시크릿 키 설정
  public JwtTokenProvider(@Value("${spring.jwt.secret}") String secret,
      UserDetailsServiceImpl userDetailsService, RedisService redisService) {
    this.userDetailsService = userDetailsService;
    this.redisService = redisService;
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    key = Keys.hmacShaKeyFor(keyBytes);
  }

  // AT 생성
  public String generateAccessToken(String email, String role) {
    Date now = new Date();
    Date accessTokenExpiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

    String accessToken = Jwts.builder()
        .setExpiration(accessTokenExpiration)
        .setSubject("access-token")
        .claim("email", email)
        .claim("role", role)
        .signWith(key, SignatureAlgorithm.HS512)
        .compact();

    log.info("[JwtTokenProvider] : accessToken 생성 완료");

    return accessToken;
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

    log.info("[JwtTokenProvider] : accessToken, refreshToken 생성 완료");

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
    log.info("[JwtTokenProvider] 토큰 인증 정보 조회 완료, userName : {}", userDetails.getUsername());
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  /**
   * 토큰 검증
   */
  public boolean validateToken(String token) {
    try {
      if (redisService.getData(token) != null // NPE 방지
          && redisService.getData(token).equals("logout")) { // 로그아웃 했을 경우
        return false;
      }
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.error("[JwtTokenProvider] Invalid JWT Token : {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("[JwtTokenProvider] Expired JWT Token : {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("[JwtTokenProvider] Unsupported JWT Token : {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("[JwtTokenProvider] JWT claims string is empty : {}", e.getMessage());
    }
    return false;
  }

  // 토큰 만료 시간 확인
  public long getTokenExpirationTime(String token) {
    if (token.startsWith(TOKEN_PREFIX)) {
      token = token.substring(TOKEN_PREFIX.length());
    }
    return getClaims(token).getExpiration().getTime();
  }
}
