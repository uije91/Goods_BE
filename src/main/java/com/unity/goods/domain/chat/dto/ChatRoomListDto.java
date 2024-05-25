package com.unity.goods.domain.chat.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.unity.goods.domain.chat.entity.ChatRoom;
import com.unity.goods.domain.goods.entity.Goods;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatRoomListDto {

  private final Long roomId;
  private final Long goodsId;
  private final String goodsName;
  private final String goodsImage;
  private final Long goodsPrice;
  private final String sender;
  private final String receiver;
  private final int notRead;
  private final String lastMessage;
  private final LocalDateTime updatedAt;

  public static ChatRoomListDto to(ChatRoom chatRoom, int count, String lastMessage, String sender) {
    String image = Optional.ofNullable(chatRoom)
        .map(ChatRoom::getGoods)
        .map(Goods::getImageList)
        .filter(list -> !list.isEmpty())
        .map(list -> list.get(0).getImageUrl())
        .orElse(null);

    return ChatRoomListDto.builder()
        .roomId(Objects.requireNonNull(chatRoom).getId())
        .goodsId(chatRoom.getGoods().getId())
        .goodsName(chatRoom.getGoods().getGoodsName())
        .goodsImage(image)
        .goodsPrice(chatRoom.getGoods().getPrice())
        .sender(sender)
        .receiver(chatRoom.getGoods().getMember().getNickname())
        .notRead(count)
        .lastMessage(lastMessage)
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
