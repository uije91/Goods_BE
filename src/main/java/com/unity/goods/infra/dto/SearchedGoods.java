package com.unity.goods.infra.dto;

import com.unity.goods.infra.document.GoodsDocument;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchedGoods {

  private long goodsId;
  private String sellerNickName;
  private String goodsName;
  private Long price;
  private String tradeSpot;
  private String thumbnailUrl;
  private double lat;
  private double lng;
  private Integer uploadedBefore;

  public static SearchedGoods fromGoodsDocument(GoodsDocument goodsDocument) {
    return SearchedGoods.builder()
        .goodsId(goodsDocument.getId())
        .sellerNickName(goodsDocument.getSellerNickName())
        .goodsName(goodsDocument.getGoodsName())
        .price(goodsDocument.getPrice())
        .tradeSpot(goodsDocument.getAddress())
        .thumbnailUrl(goodsDocument.getThumbnailUrl())
        .uploadedBefore(goodsDocument.getUploadedBefore())
        .lat(goodsDocument.getLocation().lat())
        .lng(goodsDocument.getLocation().lon())
        .build();
  }

}
