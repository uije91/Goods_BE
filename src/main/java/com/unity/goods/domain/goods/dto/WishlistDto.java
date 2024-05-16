package com.unity.goods.domain.goods.dto;

import com.unity.goods.domain.goods.type.GoodsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistDto {
  private Long goodsId;
  private String goodsName;
  private String sellerName;
  private String price;
  private String imageUrl;
  private GoodsStatus goodsStatus;
  private Long uploadBefore;
  private String address;
}
