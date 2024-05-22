package com.unity.goods.domain.member.dto;

import com.unity.goods.domain.member.entity.Member;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public class SignUpDto {

  @Getter
  @Setter
  @Builder
  public static class SignUpRequest {

    private MultipartFile profileImage;

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$",
        message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,20}$",
        message = "비밀번호는 8~20자 영문,숫자,특수문자를 사용하세요.")
    private String password;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,20}$",
        message = "비밀번호는 8~20자 영문,숫자,특수문자를 사용하세요.")
    private String chkPassword;

    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
    private String nickName;

    private String phoneNumber;

    @Pattern(regexp = "^$|^[0-9]{6}$", message = "거래 비밀번호는 6자리 숫자로 작성해주세요.")
    private String tradePassword;

  }

  @Getter
  @Builder
  public static class SignUpResponse {

    private String profileImageUrl;
    private String email;
    private String nickName;
    private String phoneNumber;

    public static SignUpResponse fromMember(Member member) {
      return SignUpResponse.builder()
          .email(member.getEmail())
          .nickName(member.getNickname())
          .phoneNumber(member.getPhoneNumber())
          .profileImageUrl(member.getProfileImage())
          .build();
    }

  }

}
