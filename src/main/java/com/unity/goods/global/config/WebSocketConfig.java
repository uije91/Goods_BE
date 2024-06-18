package com.unity.goods.global.config;


import com.unity.goods.domain.chat.handler.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Value("${spring.rabbitmq.host}")
  private String rabbitmqHost;

  private final StompHandler stompHandler;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {

    registry.setPathMatcher(new AntPathMatcher("."));
    registry.setApplicationDestinationPrefixes("/pub");

    // 외부 브로커 사용
    registry.enableStompBrokerRelay("/exchange")
        .setRelayHost(rabbitmqHost)
        .setVirtualHost("/")
        .setRelayPort(61613) // RabbitMQ STOMP 기본 포트
        .setSystemLogin("guest")
        .setSystemPasscode("guest")
        .setClientLogin("guest")
        .setClientPasscode("guest");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompHandler);
  }
}
