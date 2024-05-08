package com.unity.goods.domain.goods.entity;

import com.unity.goods.domain.goods.type.GoodsStatus;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Goods extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "goods_id")
  private Long id;

  private String goodsName;
  private int price;
  private String description;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private GoodsStatus goodsStatus = GoodsStatus.SALE;

  @Builder.Default
  private double goodsStar = 0;

  private String address;

  private double lat;
  private double lng;

  @ManyToOne
  @JoinColumn(name = "member_id")
  @Column(name = "seller_id")
  private Member member;

  @OneToMany(mappedBy = "goods")
  private List<GoodsImage> goodsImageList = new ArrayList<>();
}
