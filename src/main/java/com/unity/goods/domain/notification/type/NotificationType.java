package com.unity.goods.domain.notification.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

  CHAT_RECEIVED("채팅 수신", "새로운 메시지가 수신되었습니다."),
  POINT_RECEIVED("입금 완료", "입금 내역이 확인 완료되었습니다.");

  private final String title;
  private final String body;
}
