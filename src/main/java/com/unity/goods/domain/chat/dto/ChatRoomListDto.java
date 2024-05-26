package com.unity.goods.domain.chat.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatRoomListDto {

  private Long roomId;
  private String partner;
  private String profileImage;
  private String goodsImage;
  private int notRead;
  private String lastMessage;
  private LocalDateTime updatedAt;
  private Long uploadedBefore;

}
