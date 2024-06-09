package com.unity.goods.domain.notification.service;

import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.notification.dto.FcmTokenDto;
import com.unity.goods.domain.notification.type.NotificationContent;
import com.unity.goods.global.jwt.UserDetailsImpl;
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

  public void sendNotification(String token, String title, String body) throws FirebaseMessagingException {
    Message message = Message.builder()
        .setToken(token)
        .setNotification(Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build())
        .build();

    FirebaseMessaging.getInstance().send(message);
  }

  public void sendChatNotification(String token, String chatMessage) throws FirebaseMessagingException {
    NotificationContent content = NotificationContent.CHAT_RECEIVED;
    sendNotification(token, content.getTitle(), chatMessage);
  }

  public void sendTradeCompleteNotification(String token) throws FirebaseMessagingException {
    NotificationContent content = NotificationContent.TRADE_COMPLETED;
    sendNotification(token, content.getTitle(), content.getBody());
  }

  public void sendPointReceivedNotification(String token) throws FirebaseMessagingException {
    NotificationContent content = NotificationContent.POINT_RECEIVED;
    sendNotification(token, content.getTitle(), content.getBody());
  }
}
