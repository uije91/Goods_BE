package com.unity.goods.domain.goods.dto;

import com.unity.goods.domain.goods.entity.Goods;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public class UploadGoodsDto {

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  public static class UploadGoodsRequest {

    private List<MultipartFile> goodsImageFiles;

    @NotBlank(message = "상품 가격을 입력해주세요.")
    @Pattern(regexp = "^[1-9][0-9]*$", message = "가격은 0으로 시작하지 않는 숫자로 입력해야 합니다.")
    private String price;

    @NotBlank(message = "상품명을 입력해주세요.")
    private String goodsName; // 프론트에선 제목

    @Size(max = 500)
    @NotBlank(message = "상품에 대한 상세 설명을 입력해주세요.")
    private String description;

    private String userDefinedLocation; // 사용자가 적은 추가 상세 주소

    // 카카오맵에서 추출한 정보
    @NotBlank
    private String address;
    @NotNull
    private double lat; // 위도
    @NotNull
    private double lng; // 경도

  }

  @Getter
  @Builder
  public static class UploadGoodsResponse {
    private String sellerName;
    private String goodsName;
    private String price;
    private String description;
    private String address;
    private LocalDateTime createdAt;

    public static UploadGoodsResponse fromGoods(Goods goods) {
      return UploadGoodsResponse.builder()
          .sellerName(goods.getMember().getNickname())
          .goodsName(goods.getGoodsName())
          .price(String.valueOf(goods.getPrice()))
          .description(goods.getDescription())
          .createdAt(goods.getCreatedAt())
          .build();
    }
  }

}
