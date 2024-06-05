package com.unity.goods.domain.goods.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WishlistDto {
  private Long goodsId;
  private String goodsName;
  private String sellerName;
  private String price;
  private String goodsThumbnail;
  private String goodsStatus;
  private Long uploadedBefore;
  private String address;
}
