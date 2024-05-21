package com.unity.goods.domain.chat.dto;

import com.unity.goods.domain.chat.entity.ChatLog;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatLogDto {

  private String message;
  private String sender;
  private String receiver;
  private LocalDateTime createdAt;

  public ChatLogDto(ChatLog chatLog) {
    this.message = chatLog.getMessage();
    this.sender = chatLog.getSender();
    this.receiver = chatLog.getReceiver();
    this.createdAt = LocalDateTime.now();
  }
}
