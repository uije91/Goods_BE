package com.unity.goods.domain.notification.dto;

import com.unity.goods.domain.notification.type.NotificationContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmRequestDto {

  private String token;
  private NotificationContent notificationContent;

}
