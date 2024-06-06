package com.unity.goods.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Slf4j
public class FcmConfig {

  // Firebase 기능 중 FCM만 쓸 것이라 Messaging 형태로 반환하게 설정
  @Bean
  public FirebaseMessaging firebaseMessaging() throws IOException {
    // class path에서 서비스 계정 키 JSON 파일 로드
    ClassPathResource resource = new ClassPathResource("firebase/firebase_service_key.json");
    InputStream serviceAccount = resource.getInputStream();

    FirebaseApp firebaseApp;
    // 현재 애플리케이션에서 초기화된 FirebaseApp 인스턴스 목록 가져옴
    List<FirebaseApp> firebaseAppList = FirebaseApp.getApps();

    if(firebaseAppList.isEmpty()) {
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
