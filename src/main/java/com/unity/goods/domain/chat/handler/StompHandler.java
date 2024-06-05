package com.unity.goods.domain.chat.handler;

import static com.unity.goods.global.exception.ErrorCode.UNAUTHORIZED;

import com.unity.goods.global.exception.JwtFilterAuthenticationException;
import com.unity.goods.global.jwt.JwtTokenProvider;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompHandler implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    // 웹소켓 연결시 header 의 유효성 검증
    if (StompCommand.CONNECT == accessor.getCommand()) {
      String token = Objects.requireNonNull(
          accessor.getFirstNativeHeader("Authorization")).substring(7);

      if(!jwtTokenProvider.validateToken(token)) {
        throw new JwtFilterAuthenticationException(UNAUTHORIZED);
      }
    }

    return message;
  }
}
