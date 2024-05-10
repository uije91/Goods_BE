package com.unity.goods.infra.document;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.type.GoodsStatus;
import jakarta.persistence.Id;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "keywords")
public class GoodsDocument {

  // 상세 상품 조회를 위한 goodsId
  @Id
  @Field(type = FieldType.Long)
  private Long id;

  // 검색 영역 & 결과에 쓰임
  @Field(type = FieldType.Text)
  private String goodsName;

  @Field(type = FieldType.Text)
  private String description;

  @Field(type = FieldType.Text)
  private GoodsStatus goodsStatus;

  // 검색 영역에 쓰임
  @Field(type = FieldType.Text)
  private String address;

  // 검색 결과에 쓰임
  @Field(type = FieldType.Text)
  private String sellerNickName;

  @Field(type = FieldType.Long)
  private Long price;

  @Field(type = FieldType.Text)
  private String thumbnailUrl;

  @Field(type = FieldType.Integer)
  private Integer uploadedBefore;

  public static GoodsDocument fromGoods(Goods goods, String thumbnailUrl) {

    int minutesAgo = (int) Duration.between(goods.getCreatedAt(), LocalDateTime.now()).toMinutes();

    return GoodsDocument.builder()
        .id(goods.getId())
        .goodsName(goods.getGoodsName())
        .description(goods.getDescription())
        .goodsStatus(goods.getGoodsStatus())
        .sellerNickName(goods.getMember().getNickname())
        .address(goods.getAddress())
        .price(goods.getPrice())
        .thumbnailUrl(thumbnailUrl)
        .uploadedBefore(minutesAgo)
        .build();
  }

}
