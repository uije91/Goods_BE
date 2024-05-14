package com.unity.goods.domain.member.dto;

import com.unity.goods.domain.member.entity.Badge;
import com.unity.goods.domain.member.entity.Member;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class SellerProfileDto {

  @Getter
  @Builder
  public static class SellerProfileResponse {

    private String nickName;
    private String profileImage;
    private double star;
    private List<Badge> badgeList;

    public static SellerProfileResponse fromMember(Member member) {

      return SellerProfileResponse.builder()
          .nickName(member.getNickname())
          .profileImage(member.getProfileImage())
          .star(member.getStar())
          .badgeList(member.getBadgeList())
          .build();
    }

  }

}
