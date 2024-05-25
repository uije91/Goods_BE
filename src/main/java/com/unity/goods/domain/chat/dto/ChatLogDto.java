package com.unity.goods.domain.chat.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.unity.goods.domain.chat.entity.ChatLog;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatLogDto {

  private String message;
  private Long senderId;
  private Long receiverId;
  private LocalDateTime createdAt;

  public ChatLogDto(ChatLog chatLog) {
    this.message = chatLog.getMessage();
    this.senderId = chatLog.getSenderId();
    this.receiverId = chatLog.getReceiverId();
    this.createdAt = LocalDateTime.now();
  }
}
