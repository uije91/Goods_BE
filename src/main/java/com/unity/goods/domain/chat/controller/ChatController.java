package com.unity.goods.domain.chat.controller;


import com.unity.goods.domain.chat.dto.ChatMessageDto;
import com.unity.goods.domain.chat.dto.ChatMessageDto.ChatMessageResponse;
import com.unity.goods.domain.chat.dto.ChatRoomDto;
import com.unity.goods.domain.chat.dto.ChatRoomDto.ChatRoomResponse;
import com.unity.goods.domain.chat.service.ChatService;
import com.unity.goods.global.jwt.JwtTokenProvider;
import com.unity.goods.global.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO 채팅 수신 알림 기능 구현

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

  private final ChatService chatService;
  private final JwtTokenProvider jwtTokenProvider;

  // 채팅 message 메서드 수정
  @MessageMapping("/message/{roomId}")
  @SendTo("/sub/message/{roomId}")
  public ChatMessageResponse messageHandler(@DestinationVariable Long roomId,
      ChatMessageDto message,
      @Header("Authorization") String authorization) {
    String token = authorization.substring(7);
    String senderEmail = jwtTokenProvider.getClaims(token).get("email").toString();
    chatService.inviteChatRoom(roomId);
    Long senderId = chatService.addChatLog(roomId, message, senderEmail);
    log.info("[ChatController] Message sent to room {}", roomId);

    return ChatMessageResponse.builder()
        .senderId(senderId)
        .message(message.getMessage())
        .build();
  }

  @PostMapping("/room/{goodsId}")
  public ResponseEntity<ChatRoomResponse> addChatRoom(@PathVariable Long goodsId,
      @AuthenticationPrincipal UserDetailsImpl user) {
    return ResponseEntity.ok(chatService.addChatRoom(goodsId, user.getId()));
  }

  @GetMapping("/room")
  public ResponseEntity<?> getChatRoomList(
      @AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok().body(chatService.getChatRoomList(principal.getId()));
  }

  @GetMapping("/{roomId}")
  public ResponseEntity<ChatRoomDto> getChatLogs(@PathVariable Long roomId,
      @AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok().body(chatService.getChatLogs(roomId, principal.getId()));
  }

  @PostMapping("room/leave/{roomId}")
  public ResponseEntity<?> leaveChatRoom(@PathVariable Long roomId,
      @AuthenticationPrincipal UserDetailsImpl principal){
    chatService.leaveChatRoom(roomId,principal.getId());
    return ResponseEntity.ok().build();
  }

}
