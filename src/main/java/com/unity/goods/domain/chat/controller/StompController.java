package com.unity.goods.domain.chat.controller;

import com.unity.goods.domain.chat.dto.ChatMessageDto;
import com.unity.goods.domain.chat.service.ChatService;
import com.unity.goods.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StompController {

  private final ChatService chatService;
  private final JwtTokenProvider jwtTokenProvider;

  // 채팅 message 메서드 수정
  @MessageMapping("/message/{roomId}")
  @SendTo("/sub/message/{roomId}")
  public ChatMessageDto messageHandler(@DestinationVariable Long roomId, ChatMessageDto message,
      @Header("Authorization") String authorization) {
    String token = authorization.substring(7);
    String senderEmail = jwtTokenProvider.getClaims(token).get("email").toString();

    chatService.addChatLog(roomId, message, senderEmail);
    log.info("[ChatController] Message sent to room {}", roomId);
    return message;
  }

}
