package com.unity.goods.domain.goods.type;

import static com.unity.goods.global.exception.ErrorCode.INVALID_GOODS_STATUS;

import com.unity.goods.domain.goods.exception.GoodsException;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoodsStatus {
  RESERVATION("예약중"),
  SALE("판매중"),
  SOLDOUT("거래완료");

  private final String description;

  public static GoodsStatus fromDescription(String description) {
    return Arrays.stream(GoodsStatus.values())
        .filter(status -> status.getDescription().equals(description))
        .findFirst()
        .orElseThrow(() -> new GoodsException(INVALID_GOODS_STATUS));
  }
}
