package com.unity.goods.domain.notification.service;

import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.notification.dto.FcmTokenDto;
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
}
