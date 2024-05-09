package com.unity.goods.domain.oauth.dto;

import java.util.Map;

public class KakaoOAuth2Response implements OAuth2Response {

  private final Map<String, Object> attributes;
  private final Map<String, Object> kakaoAccountAttributes;
  private final Map<String, Object> profileAttributes;

  public KakaoOAuth2Response(Map<String, Object> attributes) {
    this.attributes = attributes;
    this.kakaoAccountAttributes = (Map<String, Object>) attributes.get("kakao_account");
    this.profileAttributes = (Map<String, Object>) kakaoAccountAttributes.get("profile");
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getId() {
    return attributes.get("id").toString();
  }

  @Override
  public String getEmail() {
    return kakaoAccountAttributes.get("email").toString();
  }

  @Override
  public String getName() {
    return profileAttributes.get("nickname").toString();
  }

  @Override
  public String getProfileImage() {
    return profileAttributes.get("profile_image_url").toString();
  }
}
