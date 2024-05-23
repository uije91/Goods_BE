package com.unity.goods.domain.chat.controller;


import com.unity.goods.domain.chat.dto.ChatMessageDto;
import com.unity.goods.domain.chat.dto.ChatRoomDto;
import com.unity.goods.domain.chat.dto.ChatRoomDto.ChatRoomResponse;
import com.unity.goods.domain.chat.service.ChatService;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/chat")
public class ChatController {

  private final SimpMessageSendingOperations msgTemplate;
  private final ChatService chatService;

  Map<String, String> sessions = new HashMap<>();

  @EventListener(SessionConnectEvent.class)
  public void onConnect(SessionConnectEvent event) {
    String sessionId = Objects.requireNonNull(
        event.getMessage().getHeaders().get("simpSessionId")).toString();
    String headers = Objects.requireNonNull(
        event.getMessage().getHeaders().get("nativeHeaders")).toString();
    String token = headers.split("Authorization=\\[")[1].substring(7).split("]")[0];

    sessions.put(sessionId, token);
  }

  @EventListener(SessionDisconnectEvent.class)
  public void onDisconnect(SessionDisconnectEvent event) {
    sessions.remove(event.getSessionId());
  }

  @MessageMapping("/message")
  public void sendMessage(ChatMessageDto message, SimpMessageHeaderAccessor accessor) {
    String token = sessions.get(accessor.getSessionId());
    log.info("[ChatController] Received by session : {}", token);
    msgTemplate.convertAndSend("/sub/message/" + message.getRoomId(), message);
    log.info("[ChatController] Message sent to room {}", message.getRoomId());

    chatService.addChatLog(message, token);
  }

  @PostMapping("/room")
  public ResponseEntity<ChatRoomResponse> addChatRoom(@RequestParam Long goodsId,
      @AuthenticationPrincipal UserDetailsImpl user) {
    return ResponseEntity.ok(chatService.addChatRoom(goodsId, user.getId()));
  }

  @GetMapping("/room")
  public ResponseEntity<?> getChatRoomList(
      @AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok().body(chatService.getChatRoomList(principal.getId()));
  }

  @GetMapping("/{roomId}")
  public ResponseEntity<ChatRoomDto> getChatLog(@PathVariable Long roomId,
      @AuthenticationPrincipal UserDetailsImpl principal) {
    return ResponseEntity.ok().body(chatService.getChatLog(roomId, principal.getId()));
  }

  @GetMapping("/read")
  public ResponseEntity<Integer> getChatLogNotRead(
      @AuthenticationPrincipal UserDetailsImpl principal) {

    return ResponseEntity.ok(chatService.countAllChatNotRead(principal.getId()));
  }
}
