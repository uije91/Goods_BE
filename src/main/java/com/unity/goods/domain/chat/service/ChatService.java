package com.unity.goods.domain.chat.service;

import static com.unity.goods.domain.chat.chatType.ChatRole.BUYER;
import static com.unity.goods.domain.chat.chatType.ChatRole.SELLER;
import static com.unity.goods.global.exception.ErrorCode.CHAT_ROOM_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.FCM_TOKEN_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.chat.chatType.ChatRole;
import com.unity.goods.domain.chat.dto.ChatMessageDto;
import com.unity.goods.domain.chat.dto.ChatRoomDto;
import com.unity.goods.domain.chat.dto.ChatRoomDto.ChatRoomResponse;
import com.unity.goods.domain.chat.dto.ChatRoomListDto;
import com.unity.goods.domain.chat.entity.ChatLog;
import com.unity.goods.domain.chat.entity.ChatRoom;
import com.unity.goods.domain.chat.exception.ChatException;
import com.unity.goods.domain.chat.repository.ChatLogRepository;
import com.unity.goods.domain.chat.repository.ChatRoomRepository;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.notification.dto.FcmRequestDto;
import com.unity.goods.domain.notification.service.FcmService;
import com.unity.goods.domain.notification.type.NotificationContent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  private final GoodsRepository goodsRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatLogRepository chatLogRepository;
  private final MemberRepository memberRepository;
  private final FcmService fcmService;

  // 채팅방 생성
  public ChatRoomResponse addChatRoom(Long goodsId, Long buyerId) {
    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new ChatException(GOODS_NOT_FOUND));

    ChatRoom chatRoom = ChatRoom.builder()
        .sellerId(goods.getMember().getId())
        .buyerId(buyerId)
        .goods(goods)
        .updatedAt(LocalDateTime.now())
        .build();

    Optional<ChatRoom> existChatRoom = chatRoomRepository.findByBuyerIdAndSellerIdAndGoods(
        chatRoom.getBuyerId(), chatRoom.getSellerId(), goods);

    if (existChatRoom.isPresent()) {
      log.info("[ChatService] Chat room already exist");
      return ChatRoomResponse.builder().roomId(existChatRoom.get().getId()).build();
    }

    chatRoomRepository.save(chatRoom);
    return ChatRoomResponse.builder().roomId(chatRoom.getId()).build();
  }

  // 채팅방 목록 조회
  public List<ChatRoomListDto> getChatRoomList(Long memberId) {
    return chatRoomRepository.findAllByBuyerIdOrSellerId(memberId, memberId).stream()
        .filter(room -> !room.getChatLogs().isEmpty() && !hasUserLeft(room, memberId))
        .map(m -> {
          int count = countChatLogNotRead(m.getChatLogs(), memberId);
          return getChatRoomListDto(m, count, memberId);
        })
        .sorted(Comparator.comparing(ChatRoomListDto::getUpdatedAt, Comparator.reverseOrder()))
        .collect(Collectors.toList());
  }

  private ChatRoomListDto getChatRoomListDto(ChatRoom chatRoom, int count, Long memberId) {
    Long partnerId = chatRoomRepository.findOppositeMemberId(chatRoom.getId(), memberId);
    Member partner = memberRepository.findById(partnerId)
        .orElseThrow(() -> new ChatException(USER_NOT_FOUND));

    String lastMessage = chatRoom.getChatLogs().get(chatRoom.getChatLogs().size() - 1).getMessage();
    LocalDateTime lastMessageTime = chatRoom.getChatLogs().get(chatRoom.getChatLogs().size() - 1)
        .getCreatedAt();

    long uploadedBefore = Duration.between(lastMessageTime, LocalDateTime.now()).getSeconds();
    String goodsImage = Optional.ofNullable(chatRoom.getGoods())
        .map(Goods::getImageList)
        .filter(list -> !list.isEmpty())
        .map(list -> list.get(0).getImageUrl())
        .orElse(null);

    return ChatRoomListDto.builder()
        .goodsImage(goodsImage)
        .roomId(chatRoom.getId())
        .partner(partner.getNickname())
        .profileImage(partner.getProfileImage())
        .notRead(count)
        .lastMessage(lastMessage)
        .updatedAt(lastMessageTime)
        .uploadedBefore(uploadedBefore)
        .build();
  }

  private boolean hasUserLeft(ChatRoom chatRoom, Long memberId) {
    if (chatRoom.getBuyerId().equals(memberId)) {
      return chatRoom.isBuyerLeft();
    } else if (chatRoom.getSellerId().equals(memberId)) {
      return chatRoom.isSellerLeft();
    }
    return false;
  }

  // 채팅방에서 읽지 않은 채팅의 수
  private int countChatLogNotRead(List<ChatLog> chatLogs, Long memberId) {

    int result = 0;
    for (ChatLog chatLog : chatLogs) {
      if (!chatLog.isChecked() && memberId.equals(chatLog.getReceiverId())) {
        result++;
      }
    }
    return result;
  }

  // 채팅 내용 확인
  public ChatRoomDto getChatLogs(Long roomId, Long memberId) {

    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new ChatException(CHAT_ROOM_NOT_FOUND));

    changeChatLogAllRead(roomId, memberId);

    Long partnerId = chatRoomRepository.findOppositeMemberId(roomId, memberId);

    Member partner = memberRepository.findById(partnerId)
        .orElseThrow(() -> new ChatException(USER_NOT_FOUND));

    ChatRole chatRole = (memberId.equals(chatRoom.getBuyerId())) ? BUYER : SELLER;

    return ChatRoomDto.to(chatRoom, memberId, partner.getNickname(), chatRole);
  }

  private void changeChatLogAllRead(Long roomId, Long memberId) {
    List<ChatLog> list =
        chatLogRepository.findAllByChatRoomIdAndCheckedAndReceiverId(roomId, false, memberId);
    list.forEach(ChatLog::changeCheckedState);
    chatLogRepository.saveAll(list);
  }

  // 채팅로그 저장
  @Transactional
  public Long addChatLog(Long roomId, ChatMessageDto chatMessageDto, String senderEmail)
      throws Exception {
    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new ChatException(CHAT_ROOM_NOT_FOUND));

    Long senderId = memberRepository.findMemberByEmail(senderEmail).getId();
    Long receiverId =
        (Objects.equals(senderId, chatRoom.getBuyerId())) ? chatRoom.getSellerId()
            : chatRoom.getBuyerId();

    LocalDateTime localDateTime = LocalDateTime.now();

    chatRoom.changeDate(localDateTime);
    ChatLog chatLog = ChatLog.builder()
        .senderId(senderId)
        .receiverId(receiverId)
        .chatRoom(chatRoom)
        .message(chatMessageDto.getMessage())
        .createdAt(localDateTime)
        .checked(false)
        .build();

    chatLogRepository.save(chatLog);
    sendChatNotification(receiverId);

    return senderId;
  }

  private void sendChatNotification(Long receiverId) throws Exception {
    Member receiver = memberRepository.findById(receiverId)
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    if (receiver.getFcmToken() != null && !receiver.getFcmToken().isEmpty()) {
      FcmRequestDto fcmRequestDto = FcmRequestDto.builder()
          .token(receiver.getFcmToken())
          .notificationContent(NotificationContent.CHAT_RECEIVED)
          .build();

      fcmService.requestNotificationToFcm(fcmRequestDto);
    } else {
      throw new MemberException(FCM_TOKEN_NOT_FOUND);
    }
  }

  // 채팅방 나가기
  @Transactional
  public void leaveChatRoom(Long roomId, Long memberId) {
    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new ChatException(CHAT_ROOM_NOT_FOUND));

    if (chatRoom.getBuyerId().equals(memberId)) {
      chatRoom.setBuyerLeft(true);
    } else if (chatRoom.getSellerId().equals(memberId)) {
      chatRoom.setSellerLeft(true);
    } else {
      throw new ChatException(USER_NOT_FOUND);
    }
    chatRoomRepository.save(chatRoom);
  }

  // 대화시 나간 사용자가 다시 대화에 참여, 게시글 목록 재출력
  public void inviteChatRoom(Long roomId) {
    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new ChatException(CHAT_ROOM_NOT_FOUND));

    if (chatRoom.isSellerLeft() || chatRoom.isBuyerLeft()) {
      chatRoom.setSellerLeft(false);
      chatRoom.setBuyerLeft(false);
      chatRoomRepository.save(chatRoom);
    }
  }
}
