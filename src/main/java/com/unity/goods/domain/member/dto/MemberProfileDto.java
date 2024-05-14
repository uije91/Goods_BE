package com.unity.goods.domain.member.dto;

import com.unity.goods.domain.member.entity.Badge;
import com.unity.goods.domain.member.entity.Member;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class MemberProfileDto {

  @Getter
  @Builder
  public static class MemberProfileResponse {

    private String nickName;
    private String phoneNumber;
    private String profileImage;
    private double star;
    private List<Badge> badgeList;


    public static MemberProfileResponse fromMember(Member member) {

      return MemberProfileResponse.builder()
          .nickName(member.getNickname())
          .phoneNumber(member.getPhoneNumber())
          .profileImage(member.getProfileImage())
          .star(member.getStar())
          .badgeList(member.getBadgeList())
          .build();
    }

  }

}
