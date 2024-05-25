package com.unity.goods.domain.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unity.goods.domain.chat.dto.ChatRoomDto;
import com.unity.goods.domain.chat.dto.ChatRoomListDto;
import com.unity.goods.domain.chat.entity.ChatLog;
import com.unity.goods.domain.chat.entity.ChatRoom;
import com.unity.goods.domain.chat.repository.ChatLogRepository;
import com.unity.goods.domain.chat.repository.ChatRoomRepository;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.repository.GoodsRepository;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @InjectMocks
  private ChatService chatService;

  @Mock
  private ChatRoomRepository chatRoomRepository;
  @Mock
  private ChatLogRepository chatLogRepository;
  @Mock
  private GoodsRepository goodsRepository;
  @Mock
  private MemberRepository memberRepository;

  private Member member;
  private Member member2;
  private Goods goods;

  @BeforeEach
  void setUp() {
    member = Member.builder()
        .id(10L)
        .nickname("판매자")
        .build();

    member2 = Member.builder()
        .id(12L)
        .nickname("구매자")
        .build();

    goods = Goods.builder()
        .id(1L)
        .goodsName("테스트제품")
        .member(member)
        .build();
  }

  @Test
  @DisplayName("채팅방 생성")
  void addChatRoom_Test() throws Exception {
    // given
    UserDetailsImpl user = new UserDetailsImpl(member2);
    when(goodsRepository.findById(anyLong())).thenReturn(Optional.of(goods));

    // when
    chatService.addChatRoom(goods.getId(), user.getId());

    // then
    verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
  }

  @Test
  @DisplayName("채팅방 목록 조회")
  void getChatRoomList_Test() throws Exception {
    // given
    List<ChatLog> chatLogList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      ChatLog chatLog = ChatLog.builder()
          .senderId(member2.getId())
          .receiverId(member.getId())
          .message("msg" + i)
          .build();
      chatLogList.add(chatLog);
    }

    ChatRoom chatRoom = ChatRoom.builder()
        .id(1L)
        .goods(goods)
        .sellerId(member.getId())
        .buyerId(member2.getId())
        .chatLogs(chatLogList)
        .build();

    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member2));
    when(chatRoomRepository.findAllByBuyerIdOrSellerId(anyLong(), anyLong()))
        .thenReturn(List.of(chatRoom));

    // when
    List<ChatRoomListDto> result = chatService.getChatRoomList(member2.getId());

    //then
    assertEquals(1, result.size());
    assertEquals("구매자", result.get(0).getSender());

    verify(chatRoomRepository, times(1))
        .findAllByBuyerIdOrSellerId(12L, 12L);
  }

  @Test
  @DisplayName("채팅내역 조회")
  void getChatLog_Test() throws Exception{
    // given
    List<ChatLog> chatLogList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      ChatLog chatLog = ChatLog.builder()
          .senderId(member2.getId())
          .receiverId(member.getId())
          .message("msg" + i)
          .build();

      chatLogList.add(chatLog);
    }

    ChatRoom chatRoom = ChatRoom.builder()
        .id(1L)
        .goods(goods)
        .sellerId(member.getId())
        .buyerId(member2.getId())
        .chatLogs(chatLogList)
        .build();



    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member2));
    when(chatRoomRepository.findById(anyLong())).thenReturn(Optional.of(chatRoom));

    // when
    ChatRoomDto chatRoomDto = chatService.getChatLogs(chatRoom.getId(),member2.getId());

    //then
    assertEquals(5, chatRoomDto.getChatLogs().size());
    assertEquals("msg4", chatRoomDto.getChatLogs().get(4).getMessage());
    assertEquals(1L, chatRoomDto.getGoodsId());

    verify(chatRoomRepository, times(1)).findById(anyLong());
  }
}