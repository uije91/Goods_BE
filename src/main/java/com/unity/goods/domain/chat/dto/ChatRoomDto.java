package com.unity.goods.domain.chat.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.unity.goods.domain.chat.chatType.ChatRole;
import com.unity.goods.domain.chat.entity.ChatRoom;
import com.unity.goods.domain.goods.entity.Goods;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatRoomDto {

  private final Long roomId;
  private final Long goodsId;
  private final Long memberId;
  private final String partner;
  private final ChatRole memberType;
  private final String goodsName;
  private final String goodsImage;
  private final Long goodsPrice;
  private final List<ChatLogDto> chatLogs;

  public static ChatRoomDto to(ChatRoom chatRoom, Long memberId, String partner, ChatRole chatRole) {
    String image = Optional.ofNullable(chatRoom)
        .map(ChatRoom::getGoods)
        .map(Goods::getImageList)
        .filter(list -> !list.isEmpty())
        .map(list -> list.get(0).getImageUrl())
        .orElse(null);

    return ChatRoomDto.builder()
        .roomId(Objects.requireNonNull(chatRoom).getId())
        .goodsId(chatRoom.getGoods().getId())
        .memberId(memberId)
        .partner(partner)
        .memberType(chatRole)
        .goodsName(chatRoom.getGoods().getGoodsName())
        .goodsImage(image)
        .goodsPrice(chatRoom.getGoods().getPrice())
        .chatLogs(chatRoom.getChatLogs().stream()
            .map(ChatLogDto::new)
            .collect(Collectors.toList()))
        .build();

  }

  @Getter
  @Builder
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class ChatRoomResponse {

    private Long roomId;
  }

}