package com.unity.goods.domain.goods.dto;

import com.unity.goods.domain.goods.type.GoodsStatus;
import lombok.Builder;
import lombok.Getter;

public class SellerSalesListDto {

  @Getter
  @Builder
  public static class SellerSalesListResponse {

    private Long goodsId;
    private String sellerName;
    private String goodsName;
    private String price;
    private String goodsThumbnail;
    private GoodsStatus goodsStatus;
    private Long uploadedBefore;
  }
}
