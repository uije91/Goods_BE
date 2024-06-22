package com.unity.goods.domain.goods.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ClusterDto {

  // 중심
  private double baseLat;
  private double baseLng;

  // 최대 갯수
  private int quantity;

  // 북동쪽
  private double neLat;
  private double neLng;

  // 남서쪽
  private double swLat;
  private double swLng;

}
