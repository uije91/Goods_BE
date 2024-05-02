package com.unity.goods.domain.member.entity;

import static com.unity.goods.domain.member.type.SocialType.SERVER;
import static com.unity.goods.domain.member.type.Status.ACTIVE;
import static com.unity.goods.domain.member.type.Status.INACTIVE;

import com.unity.goods.domain.member.dto.SignUpDto.SignUpRequest;
import com.unity.goods.domain.member.type.Role;
import com.unity.goods.domain.member.type.SocialType;
import com.unity.goods.domain.member.type.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;
  private String nickname;

  @Column(unique = true)
  private String email;
  private String password;

  @Column(unique = true)
  private String phoneNumber;
  private String profileImage;

  @Builder.Default
  private double star = 0;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Status status = INACTIVE;

  private String tradePassword;

  @Enumerated(EnumType.STRING)
  private SocialType socialType;

  public static Member fromSignUpRequest(SignUpRequest signUpRequest, String imageUrl) {

    return Member.builder()
        .nickname(signUpRequest.getNickName())
        .email(signUpRequest.getEmail())
        .password(signUpRequest.getPassword())
        .phoneNumber(signUpRequest.getPhoneNumber())
        .profileImage(imageUrl)
        .role(Role.USER)
        .status(ACTIVE)
        .tradePassword(signUpRequest.getTradePassword())
        .socialType(SERVER)
        .build();
  }

  public void inactivateStatus() {
    this.status = INACTIVE;
  }

}
