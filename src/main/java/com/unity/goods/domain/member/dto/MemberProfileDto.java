package com.unity.goods.domain.member.dto;

import static com.unity.goods.domain.member.entity.Badge.badgeToString;
import com.unity.goods.domain.member.entity.Member;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public class MemberProfileDto {

  @Getter
  @Builder
  public static class MemberProfileResponse {

    private Long memberId;
    private String nickName;
    private String phoneNumber;
    private String profileImage;
    private double star;
    private List<String> badgeList;

    public static MemberProfileResponse fromMember(Member member) {

      return MemberProfileResponse.builder()
          .memberId(member.getId())
          .nickName(member.getNickname())
          .phoneNumber(member.getPhoneNumber())
          .profileImage(member.getProfileImage())
          .star(member.getStar())
          .badgeList(badgeToString(member.getBadgeList()))
          .build();
    }

  }

}
