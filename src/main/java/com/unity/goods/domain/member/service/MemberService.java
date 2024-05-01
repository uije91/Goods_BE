package com.unity.goods.domain.member.service;

import static com.unity.goods.global.exception.ErrorCode.CURRENT_USED_PASSWORD;
import static com.unity.goods.global.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.unity.goods.domain.member.dto.ChangePasswordDto.ChangePasswordRequest;
import com.unity.goods.domain.member.dto.SignUpRequest;
import com.unity.goods.domain.member.dto.SignUpResponse;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public SignUpResponse signUpMember(SignUpRequest signUpRequest) {
    // 해당 유저가 이메일 인증이 된 사람인지 확인

    return null;
  }

  @Transactional
  public void changePassword(ChangePasswordRequest changePasswordRequest,
      Member member) {
    Member findMember = memberRepository.findByEmail(member.getEmail())
        .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));

    if (passwordEncoder.matches(changePasswordRequest.getPassword(), member.getPassword())) {
      throw new MemberException(CURRENT_USED_PASSWORD);
    }

    findMember.changePassword(passwordEncoder.encode(changePasswordRequest.getPassword()));

  }
}
