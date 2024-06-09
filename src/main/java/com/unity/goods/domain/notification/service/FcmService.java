package com.unity.goods.domain.notification.service;

import static com.unity.goods.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Notification;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.notification.dto.FcmRequestDto;
import com.unity.goods.domain.notification.dto.FcmTokenDto;
import com.unity.goods.domain.notification.type.NotificationContent;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

  private final MemberRepository memberRepository;

  public void registerFcmToken(UserDetailsImpl member, FcmTokenDto fcmTokenDto) {
    Member savedMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    savedMember.setFcmToken(fcmTokenDto.getFcmToken());

    memberRepository.save(savedMember);
    log.info("[FcmService] : {} fcm 토큰 저장 완료", savedMember.getEmail());
  }

  private Message makeMessage(String token, NotificationContent content) {
    Notification notification = Notification.builder()
        .setTitle(content.getTitle())
        .setBody(content.getBody())
        .build();

    return Message.builder()
        .setToken(token)
        .setNotification(notification)
        .build();
  }

  public void requestNotificationToFcm(FcmRequestDto fcmRequestDto) throws Exception {
    Message message = makeMessage(fcmRequestDto.getToken(), fcmRequestDto.getNotificationContent());

    try {
      // FCM 서버로 메시지 전송
      String response = FirebaseMessaging.getInstance().send(message);
      log.info("[FcmService]: 전송 완료 : {} ", response);
    } catch (Exception e) {
      throw new Exception(String.valueOf(INTERNAL_SERVER_ERROR));
    }

  }
}
