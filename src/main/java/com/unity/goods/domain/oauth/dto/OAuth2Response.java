package com.unity.goods.domain.oauth.dto;

import java.util.Map;

public interface OAuth2Response {

  Map<String, Object> getAttributes();
  String getId();
  String getEmail();
  String getName();
  String getProfileImage();
}
