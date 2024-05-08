package com.unity.goods.domain.goods.dto;

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
  public static class GoodsDetailResponse {
    private String sellerName;
    private boolean sellerBadge; // TODO
    private boolean mannerBadge; // TODO
    private String goodsName;
    private String price;
    private String description;
    private List<String> goodsImages;
    private String status;
    private boolean liked; // TODO
    private Duration uploadedBefore;

    public static GoodsDetailResponse fromGoodsAndMember(Goods goods, Member member){
      Duration duration = Duration.between(goods.getCreatedAt(), LocalDateTime.now());

      return GoodsDetailResponse.builder()
          .sellerName(goods.getMember().getNickname())
          .goodsName(goods.getGoodsName())
          .price(String.valueOf(goods.getPrice()))
          .description(goods.getDescription())
          .status(goods.getGoodsStatus().toString())
          .uploadedBefore(duration)
          .build();
    }
  }

}
