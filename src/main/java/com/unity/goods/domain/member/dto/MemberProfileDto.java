package com.unity.goods.domain.member.dto;

import com.unity.goods.domain.member.entity.Member;
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

    public static MemberProfileResponse fromMember(Member member) {

      return MemberProfileResponse.builder()
          .nickName(member.getNickname())
          .phoneNumber(member.getPhoneNumber())
          .profileImage(member.getProfileImage())
          .star(member.getStar())
          .build();
    }

  }

}
