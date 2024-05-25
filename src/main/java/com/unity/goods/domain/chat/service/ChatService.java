package com.unity.goods.domain.chat.service;

import static com.unity.goods.global.exception.ErrorCode.ALREADY_GENERATED_CHAT_ROOM;
import static com.unity.goods.global.exception.ErrorCode.CHAT_ROOM_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.GOODS_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

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
import com.unity.goods.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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

  // 채팅방 생성
  public ChatRoomResponse addChatRoom(Long goodsId, Long buyerId) {
    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new ChatException(GOODS_NOT_FOUND));

    // 구매자와 상품 아이디로 채팅방 존재하는지 확인
    if (chatRoomRepository.existsByBuyerIdAndGoodsId(buyerId, goodsId)) {
      throw new ChatException(ALREADY_GENERATED_CHAT_ROOM);
    }

    ChatRoom chatRoom = ChatRoom.builder()
        .sellerId(goods.getMember().getId())
        .buyerId(buyerId)
        .goods(goods)
        .updatedAt(LocalDateTime.now())
        .build();

    chatRoomRepository.save(chatRoom);

    return ChatRoomResponse.builder()
        .roomId(chatRoom.getId())
        .build();
  }

  // 채팅방 목록 조회
  public List<ChatRoomListDto> getChatRoomList(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new ChatException(USER_NOT_FOUND));

    return chatRoomRepository.findAllByBuyerIdOrSellerId(memberId, memberId).stream()
        .filter(room -> !room.getChatLogs().isEmpty())
        .map(m -> {
          int count = countChatLogNotRead(m.getChatLogs(), memberId);
          String lastMessage = m.getChatLogs().get(m.getChatLogs().size() - 1).getMessage();
          return ChatRoomListDto.to(m, count, lastMessage, member.getNickname());
        })
        .sorted(Comparator.comparing(ChatRoomListDto::getUpdatedAt, Comparator.reverseOrder()))
        .collect(Collectors.toList());
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

    return ChatRoomDto.to(chatRoom);
  }

  private void changeChatLogAllRead(Long roomId, Long memberId) {
    List<ChatLog> list =
        chatLogRepository.findAllByChatRoomIdAndCheckedAndReceiverId(roomId, false, memberId);
    list.forEach(ChatLog::changeCheckedState);
    chatLogRepository.saveAll(list);
  }

  // 채팅로그 저장
  @Transactional
  public void addChatLog(Long roomId, ChatMessageDto chatMessageDto, String senderEmail) {
    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new ChatException(CHAT_ROOM_NOT_FOUND));

    Long senderId = memberRepository.findMemberByEmail(senderEmail).getId();
    Long receiverId =
        (senderId == chatRoom.getBuyerId()) ? chatRoom.getSellerId() : chatRoom.getBuyerId();

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
  }
}
