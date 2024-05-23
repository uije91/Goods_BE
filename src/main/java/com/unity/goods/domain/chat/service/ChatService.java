package com.unity.goods.domain.chat.service;

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
import com.unity.goods.global.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
  private final JwtTokenProvider jwtTokenProvider;

  // 채팅방 생성
  public ChatRoomResponse addChatRoom(Long goodsId, Long memberId) {
    Goods goods = goodsRepository.findById(goodsId)
        .orElseThrow(() -> new ChatException(GOODS_NOT_FOUND));

    ChatRoom chatRoom = ChatRoom.builder()
        .sellerId(goods.getMember().getId())
        .buyerId(memberId)
        .goods(goods)
        .updatedAt(LocalDateTime.now())
        .build();

    Optional<ChatRoom> existChatRoom = chatRoomRepository.findByBuyerIdAndSellerIdAndGoods(
        chatRoom.getBuyerId(), chatRoom.getSellerId(), goods);

    if (existChatRoom.isPresent()) {

      log.info("[ChatService][addChatRoom]: 이미 존재하는 채팅방" + "goods={}, sellerId={}, buyerId={}",
          goods, chatRoom.getSellerId(), chatRoom.getBuyerId());
      return ChatRoomResponse.builder()
          .roomId(existChatRoom.get().getId())
          .build();
    }

    chatRoomRepository.save(chatRoom);

    return ChatRoomResponse.builder()
        .roomId(chatRoom.getId())
        .build();
  }

  // 채팅방 목록 조회
  public List<ChatRoomListDto> getChatRoomList(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new ChatException(USER_NOT_FOUND));
    String sender = member.getNickname();

    return chatRoomRepository.findAllByBuyerIdOrSellerId(memberId, memberId).stream()
        .filter(room -> !room.getChatLogs().isEmpty())
        .map(m -> {
          int count = countChatLogNotRead(m.getChatLogs(), member);
          String lastMessage = m.getChatLogs().get(m.getChatLogs().size() - 1).getMessage();
          return ChatRoomListDto.to(m, count, lastMessage, sender);
        })
        .sorted(Comparator.comparing(ChatRoomListDto::getUpdatedAt, Comparator.reverseOrder()))
        .collect(Collectors.toList());
  }

  // 채팅방에서 읽지 않은 채팅의 수
  private int countChatLogNotRead(List<ChatLog> chatLogs, Member member) {
    String nickname = member.getNickname();
    int result = 0;
    for (ChatLog chatLog : chatLogs) {
      if (!chatLog.isChecked() && nickname.equals(chatLog.getReceiver())) {
        result++;
      }
    }
    return result;
  }

  // 채팅 내용 확인
  public ChatRoomDto getChatLogs(Long roomId, Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new ChatException(USER_NOT_FOUND));

    changeChatLogAllRead(roomId, member.getNickname());

    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new ChatException(CHAT_ROOM_NOT_FOUND));

    return ChatRoomDto.to(chatRoom);
  }

  private void changeChatLogAllRead(Long roomId, String name) {
    List<ChatLog> list =
        chatLogRepository.findAllByChatRoomIdAndCheckedAndReceiver(roomId, false, name);
    list.forEach(ChatLog::changeCheckedState);
    chatLogRepository.saveAll(list);
  }

  // 채팅로그 저장
  @Transactional
  public void addChatLog(ChatMessageDto chatMessageDto, String token) {
    ChatRoom chatRoom = chatRoomRepository.findById(chatMessageDto.getRoomId())
        .orElseThrow(() -> new ChatException(CHAT_ROOM_NOT_FOUND));

    String email = jwtTokenProvider.getClaims(token).get("email").toString();
    String sender = memberRepository.findMemberByEmail(email).getNickname();

    String receiver;

    String buyer = "";
    Optional<Member> optionalMember = memberRepository.findById(chatRoom.getBuyerId());
    if (optionalMember.isPresent()) {
      buyer = optionalMember.get().getNickname();
    }

    String seller = chatRoom.getGoods().getMember().getNickname();

    if (seller.equals(sender)) {
      receiver = buyer;
    } else {
      receiver = seller;
    }

    String message = chatMessageDto.getMessage();

    ChatLog chatLog = ChatLog.builder()
        .sender(sender)
        .receiver(receiver)
        .chatRoom(chatRoom)
        .message(message)
        .createdAt(LocalDateTime.now())
        .build();

    chatLogRepository.save(chatLog);
    chatRoom.changeDate();
    chatLog.addChatRoom(chatRoom);
  }
}
