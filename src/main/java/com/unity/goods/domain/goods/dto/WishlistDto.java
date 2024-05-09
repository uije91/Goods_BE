package com.unity.goods.domain.goods.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistDto {

  private String sellerName;
  private String goodsName;
  private Long price;
  private String thumbnailImageUrl;
  private GoodsStatus goodsStatus;
  private String uploadBefore;
  private String address;

}
