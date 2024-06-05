package com.unity.goods.global.exception;

import static com.unity.goods.global.exception.ErrorCode.UNAUTHORIZED;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationEntryPointHandler implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    log.error("[JwtAuthenticationEntryPoint] : 인증 실패");
    setResponse(response);
  }

  private void setResponse(HttpServletResponse response)
      throws IOException {
    // HTTP 응답 response 생성
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    String jsonResponse = objectMapper.writeValueAsString(ErrorResponse.builder()
        .message(UNAUTHORIZED.getMessage())
        .status(401)
        .errorCode(UNAUTHORIZED)
        .build());

    // JSON 응답 전송
    response.getWriter().write(jsonResponse);
  }
}
