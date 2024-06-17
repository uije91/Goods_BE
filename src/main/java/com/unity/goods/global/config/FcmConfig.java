package com.unity.goods.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

@Configuration
@Slf4j
public class FcmConfig {

  // application.yml에서 Firebase 서비스 키를 환경 변수로 가져옴
  @Value("${firebase.service.key}")
  private String firebaseServiceKey;

  @Bean
  public FirebaseMessaging firebaseMessaging() throws IOException {
    // Base64로 인코딩된 JSON 키 문자열을 디코딩
    byte[] decodedKey = Base64.getDecoder().decode(firebaseServiceKey);
    InputStream serviceAccount = new ByteArrayInputStream(decodedKey);

    FirebaseApp firebaseApp;
    // 현재 애플리케이션에서 초기화된 FirebaseApp 인스턴스 목록 가져옴
    List<FirebaseApp> firebaseAppList = FirebaseApp.getApps();

    if (firebaseAppList.isEmpty()) {
      // 이전 생성 객체가 없으면 해당 서비스 계정 키로 새로 생성
      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();

      firebaseApp = FirebaseApp.initializeApp(options);

    } else {
      firebaseApp = FirebaseApp.getInstance();
    }

    return FirebaseMessaging.getInstance(firebaseApp);
  }
}
