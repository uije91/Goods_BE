package com.unity.goods.domain.member.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.unity.goods.domain.member.entity.Member;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

public class UpdateProfileDto {

  @Getter
  @Builder
  public static class UpdateProfileRequest {

    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
    private String nick_name;

    private String phone_number;

    private String profile_image_url;
    private MultipartFile profile_image_file;

  }

  @Getter
  @Builder
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class UpdateProfileResponse {

    private String nickName;
    private String phoneNumber;
    private String profileImageUrl;

    public static UpdateProfileResponse fromMember(Member member) {
      return UpdateProfileResponse.builder()
          .nickName(member.getNickname())
          .phoneNumber(member.getPhoneNumber())
          .profileImageUrl(member.getProfileImage())
          .build();
    }

  }
}
