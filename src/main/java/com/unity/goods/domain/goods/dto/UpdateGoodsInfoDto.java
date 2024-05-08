package com.unity.goods.domain.goods.dto;

import com.unity.goods.domain.goods.entity.Goods;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public class UpdateGoodsInfoDto {

  @Getter
  @Setter
  @Builder
  public static class UpdateGoodsInfoRequest {

    private String goodsName;

    @Pattern(regexp = "^[1-9][0-9]*$", message = "가격은 0으로 시작하지 않는 숫자로 입력해야 합니다.")
    private String price;

    private String description;
    private List<MultipartFile> goodsImages;
    private Double lat;
    private Double lng;
    private String detailLocation;

  }

  @Getter
  @Builder
  public static class UpdateGoodsInfoResponse {

    private String sellerName;
    private String goodsName;
    private String price;
    private String description;
    private String detailLocation;
    private LocalDateTime updatedAt;

    public static UpdateGoodsInfoResponse fromGoods(Goods goods) {
      return UpdateGoodsInfoResponse.builder()
          .sellerName(goods.getMember().getNickname())
          .goodsName(goods.getGoodsName())
          .price(String.valueOf(goods.getPrice()))
          .description(goods.getDescription())
          .detailLocation(goods.getAddress())
          .updatedAt(goods.getUpdatedAt())
          .build();
    }
  }


}
