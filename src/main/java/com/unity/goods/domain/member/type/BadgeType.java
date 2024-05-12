package com.unity.goods.domain.member.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BadgeType {
  SELL("판매왕"),
  MANNER("매너왕");

  private final String description;
}
