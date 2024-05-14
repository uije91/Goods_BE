package com.unity.goods.domain.goods.entity;

import static com.unity.goods.domain.goods.type.GoodsStatus.SALE;

import com.unity.goods.domain.goods.dto.UploadGoodsDto.UploadGoodsRequest;
import com.unity.goods.domain.goods.type.GoodsStatus;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.model.BaseEntity;
import com.unity.goods.domain.trade.entity.Trade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Goods extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "goods_id")
  private Long id;

  @Column(nullable = false, length = 50)
  private String goodsName;

  @Column(nullable = false)
  private Long price;

  @Column(nullable = false, length = 500)
  private String description;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private GoodsStatus goodsStatus = SALE;

  @Builder.Default
  private double star = 0.0;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private double lat; // 위도

  @Column(nullable = false)
  private double lng; // 경도

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @OneToMany(mappedBy = "goods")
  @Builder.Default
  private List<Image> imageList = new ArrayList<>();

  @OneToMany(mappedBy = "goods")
  private List<Trade> tradeList  = new ArrayList<>();

  public static Goods fromUploadGoodsRequest(UploadGoodsRequest request) {

    String userDefinedLocation =
        request.getUserDefinedLocation() == null ? "" : " " + request.getUserDefinedLocation();

    return Goods.builder()
        .goodsName(request.getGoodsName())
        .price(Long.parseLong(request.getPrice()))
        .description(request.getDescription())
        .address(request.getAddress() + userDefinedLocation)
        .lat(request.getLat())
        .lng(request.getLng())
        .build();
  }

}
