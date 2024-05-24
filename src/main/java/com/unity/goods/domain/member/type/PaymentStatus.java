package com.unity.goods.domain.member.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {

  SUCCESS("success"),
  FAIL("fail");

  private final String description;

}
