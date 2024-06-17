package com.unity.goods.domain.chat.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.unity.goods.domain.chat.chatType.ChatRole;
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

  @Getter
  @Builder
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class ChatRoomResponse {

    private Long roomId;
  }

}
