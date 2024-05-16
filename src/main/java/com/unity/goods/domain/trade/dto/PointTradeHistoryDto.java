package com.unity.goods.domain.trade.dto;

import com.unity.goods.domain.trade.entity.Trade;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PointTradeHistoryDto {

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PointTradeHistoryResponse {
    private String goodsId;
    private String tradePoint;
    private LocalDateTime tradeAt;
    private String tradePurpose;
  }

  public static PointTradeHistoryResponse fromTrade(Trade trade) {
    return PointTradeHistoryResponse.builder()
        .goodsId(String.valueOf(trade.getGoods().getId()))
        .tradePoint(String.valueOf(trade.getTradePoint()))
        .tradeAt(trade.getTradedAt())
        .tradePurpose(String.valueOf(trade.getTradePurpose()))
        .build();
  }

}
