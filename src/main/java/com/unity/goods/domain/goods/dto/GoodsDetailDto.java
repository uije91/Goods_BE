package com.unity.goods.domain.goods.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.member.entity.Member;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class GoodsDetailDto {

  @Getter
  @Builder
  @Setter
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class GoodsDetailResponse {

    private Long sellerId;
    private Long goodsId;
    private String sellerProfileImage;
    private String sellerName;
    private List<String> badgeList;
    private String goodsName;
    private String price;
    private String description;
    private List<String> goodsImages;
    private String status;
    private double lat;
    private double lng;
    private String address;
    private boolean liked;
    private long uploadedBefore;

    public static GoodsDetailResponse fromGoodsAndMember(Goods goods, Member member) {
      long duration = Duration.between(goods.getCreatedAt(), LocalDateTime.now()).getSeconds();

      return GoodsDetailResponse.builder()
          .sellerId(member.getId())
          .goodsId(goods.getId())
          .sellerProfileImage(member.getProfileImage())
          .sellerName(goods.getMember().getNickname())
          .goodsName(goods.getGoodsName())
          .price(String.valueOf(goods.getPrice()))
          .description(goods.getDescription())
          .status(goods.getGoodsStatus().getDescription())
          .lat(goods.getLat())
          .lng(goods.getLng())
          .address(goods.getAddress())
          .uploadedBefore(duration)
          .build();
    }
  }

}
