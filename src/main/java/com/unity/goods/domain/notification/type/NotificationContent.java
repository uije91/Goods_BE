package com.unity.goods.domain.notification.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationContent {

  CHAT_RECEIVED("채팅 수신", "새로운 메시지가 수신되었습니다."),
  POINT_RECEIVED("입금 완료", "입금 내역이 확인 완료되었습니다."),
  TRADE_COMPLETED("거래 완료", "거래가 정상적으로 이루어졌습니다.");

  private final String title;
  private final String body;
}
