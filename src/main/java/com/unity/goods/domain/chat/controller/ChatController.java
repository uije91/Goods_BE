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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO 채팅 수신 알림 기능 구현

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

  private final ChatService chatService;
  private final JwtTokenProvider jwtTokenProvider;
  private final RabbitTemplate rabbitTemplate;

  private static final String CHAT_EXCHANGE_NAME = "chat.exchange";


  //   채팅방 대화
  @MessageMapping("chat.message.{roomId}")
  public ChatMessageResponse talkUser(@DestinationVariable("roomId") Long roomId,
      @Payload ChatMessageDto message, @Header("Authorization") String authorization)
      throws Exception {

    String token = authorization.substring(7);
    String senderEmail = jwtTokenProvider.getClaims(token).get("email").toString();
    chatService.inviteChatRoom(roomId);

    Long senderId = chatService.addChatLog(roomId, message, senderEmail);

    ChatMessageResponse chatMessage = ChatMessageResponse.builder()
        .senderId(senderId)
        .message(message.getMessage())
        .build();

    rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, "room." + roomId, chatMessage);

    return chatMessage;
  }

  @PostMapping("/room/{goodsId}")
  public ResponseEntity<ChatRoomResponse> addChatRoom(@PathVariable Long goodsId,
      @AuthenticationPrincipal UserDetailsImpl user) {

    return ResponseEntity.ok(chatService.addChatRoom(goodsId, user.getId()));
  }

  @GetMapping("/room")
  public ResponseEntity<?> getChatRoomList(
      @AuthenticationPrincipal UserDetailsImpl principal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(chatService.getChatRoomList(principal.getId(), pageable));
  }

  @GetMapping("/{roomId}")
  public ResponseEntity<?> getChatLogs(@PathVariable Long roomId,
      @AuthenticationPrincipal UserDetailsImpl principal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(chatService.getChatLogs(roomId, principal.getId(), pageable));
  }

  @GetMapping("/room/{roomId}")
  public ResponseEntity<ChatRoomDto> getChatRoom(@PathVariable Long roomId,
      @AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok(chatService.getChatRoom(roomId, principal.getId()));
  }

  @PostMapping("room/leave/{roomId}")
  public ResponseEntity<?> leaveChatRoom(@PathVariable Long roomId,
      @AuthenticationPrincipal UserDetailsImpl principal) {
    chatService.leaveChatRoom(roomId, principal.getId());
    return ResponseEntity.ok().build();
  }

}
