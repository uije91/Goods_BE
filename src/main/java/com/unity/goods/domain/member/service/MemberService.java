package com.unity.goods.domain.member.service;

import com.unity.goods.domain.member.dto.LoginDto;
import com.unity.goods.domain.member.dto.SignUpRequest;
import com.unity.goods.domain.member.dto.SignUpResponse;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.SocialType;
import com.unity.goods.domain.member.type.Status;
import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.global.exception.ErrorCode;
import com.unity.goods.global.jwt.JwtTokenProvider;
import com.unity.goods.global.service.RedisService;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;
  private final RedisService redisService;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;


  public SignUpResponse signUpMember(SignUpRequest signUpRequest) {
    // 해당 유저가 이메일 인증이 된 사람인지 확인

    return null;
  }

  // 로그인
  public TokenDto login(LoginDto.LoginRequest request) {
    Optional<Member> optionalMember = memberRepository.findByEmail(request.getEmail());

    if (optionalMember.isEmpty()) {
      throw new MemberException(ErrorCode.USER_NOT_FOUND);
    }

    Member member = optionalMember.get();
    if (member.getSocialType() != SocialType.SERVER) {
      throw new MemberException(ErrorCode.USE_SOCIAL_LOGIN);
    }

    if (member.getStatus() == Status.INACTIVE) {
      throw new MemberException(ErrorCode.EMAIL_NOT_VERITY);
    }

    if (member.getStatus() == Status.RESIGN) {
      throw new MemberException(ErrorCode.RESIGN_ACCOUNT);
    }

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(ErrorCode.PASSWORD_NOT_MATCH);
    }

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
    Authentication authentication =
        authenticationManagerBuilder.getObject().authenticate(authenticationToken);

    return generateToken(authentication.getName(), getAuthorities(authentication));
  }

  // 토큰 발급
  public TokenDto generateToken(String email, String role) {
    // Redis 에 RT가 존재할 경우 -> 삭제
    if (redisService.getData("RT:" + email) != null) {
      redisService.deleteData("RT:" + email);
    }

    // AT, RT 생성 및 Redis 에 RT 저장
    TokenDto tokenDto = jwtTokenProvider.generateToken(email, role);
    redisService.setDataExpire("RT:" + email,
        tokenDto.getRefreshToken(),
        jwtTokenProvider.getTokenExpirationTime(tokenDto.getRefreshToken()));
    return tokenDto;
  }

  // 권한 이름 가져오기
  public String getAuthorities(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));
  }
}
