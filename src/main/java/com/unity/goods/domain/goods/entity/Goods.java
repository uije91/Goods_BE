package com.unity.goods.domain.goods.entity;

import static com.unity.goods.domain.goods.dto.GoodsStatus.SALE;

import com.unity.goods.domain.goods.dto.GoodsStatus;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.model.BaseEntity;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Goods extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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

  private String thumbnailImageUrl;

  @Builder.Default
  private double star = 0.0;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private double lat; // 위도

  @Column(nullable = false)
  private double lng; // 경도

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member")
  private Member member;

}
