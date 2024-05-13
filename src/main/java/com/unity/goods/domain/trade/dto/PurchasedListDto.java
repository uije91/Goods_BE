package com.unity.goods.domain.trade.dto;

import com.unity.goods.domain.goods.type.GoodsStatus;
import lombok.Builder;
import lombok.Getter;

public class PurchasedListDto {

  @Getter
  @Builder
  public static class PurchasedListResponse {

    private Long memberId;
    private Long goodsId;
    private String sellerName;
    private String goodsName;
    private String price;
    private String goodsThumbnail;
    private GoodsStatus goodsStatus;
    private Long tradedBefore;

  }
}
