package com.unity.goods.domain.goods.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoodsStatus {
  RESERVATION("예약중"),
  SALE("판매중"),
  SOLDOUT("거래완료");

  private final String description;
}
