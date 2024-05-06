package com.unity.goods.domain.oauth.service;

import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.Role;
import com.unity.goods.domain.member.type.SocialType;
import com.unity.goods.domain.member.type.Status;
import com.unity.goods.domain.oauth.dto.KakaoOAuth2Response;
import com.unity.goods.domain.oauth.dto.OAuth2Response;
import com.unity.goods.domain.oauth.exception.OAuthException;
import com.unity.goods.global.exception.ErrorCode;
import com.unity.goods.global.jwt.UserDetailsImpl;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;


  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    try {
      return this.process(userRequest, oAuth2User);
    } catch (Exception e) {
      log.error("[OAuth2UserServiceImpl][loadUser] Error: {}", e.getMessage());
      throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
    }
  }

  public OAuth2User process(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
    SocialType socialType =
        SocialType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

    OAuth2Response oAuth2Response = getOAuthResponse(socialType, oAuth2User.getAttributes());
    Member member = memberRepository.findMemberByEmail(oAuth2Response.getEmail());

    if (member != null) {
      if (member.getSocialType() == SocialType.SERVER) {
        log.error("[CustomOAuth2UserService][process] Error: 기존회원입니다. 자체 로그인을 이용해주세요.");
        throw new OAuthException(ErrorCode.USE_SERVER_LOGIN);
      }
      updateMember(member, oAuth2Response);
    } else {
      member = createMember(oAuth2Response, socialType);
    }

    return new UserDetailsImpl(member);
  }

  private OAuth2Response getOAuthResponse(SocialType socialType, Map<String, Object> attributes) {

    return switch (socialType) {
      case KAKAO -> new KakaoOAuth2Response(attributes);
      default -> throw new OAuthException(ErrorCode.INVALID_SOCIAL_TYPE);
    };
  }

  private Member createMember(OAuth2Response response, SocialType socialType) {
    // 닉네임 중복시 <닉네임_번호>로 생성
    String nickname = response.getName();
    if (memberRepository.existsByNickname(nickname)) {
      nickname += "_" + response.getId();
    }

    Member member = Member.builder()
        .nickname(nickname)
        .email(response.getEmail())
        .profileImage(response.getProfileImage())
        .role(Role.USER)
        .status(Status.ACTIVE)
        .socialType(socialType)
        .build();

    return memberRepository.save(member);
  }

  private void updateMember(Member member, OAuth2Response response) {
    if (response.getName() != null
        && !member.getNickname().equals(response.getName())
        && !memberRepository.existsByNickname(response.getName())) {
      member.setNickname(response.getName());
    }
    if (response.getProfileImage() != null
        && !member.getProfileImage().equals(response.getProfileImage())) {
      member.setProfileImage(response.getProfileImage());
    }
    memberRepository.save(member);
  }
}
