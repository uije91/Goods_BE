package com.unity.goods.domain.member.service;

import static com.unity.goods.global.exception.ErrorCode.ALREADY_REGISTERED_USER;
import static com.unity.goods.global.exception.ErrorCode.PASSWORD_NOT_MATCH;

import com.unity.goods.domain.member.dto.LoginDto;
import com.unity.goods.domain.member.dto.SignUpDto.SignUpRequest;
import com.unity.goods.domain.member.dto.SignUpDto.SignUpResponse;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.SocialType;
import com.unity.goods.domain.member.type.Status;
import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.global.exception.ErrorCode;
import com.unity.goods.global.jwt.JwtTokenProvider;
import com.unity.goods.global.service.RedisService;
import com.unity.goods.global.service.S3Service;
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
  private final S3Service s3Service;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;


  public SignUpResponse signUp(SignUpRequest signUpRequest) {
    // 비밀번호와 비밀번화 확인 일치 검사 (안전성을 위해 프론트에 이어 한번 더 검사)
    if (!signUpRequest.getPassword().equals(signUpRequest.getChkPassword())) {
      throw new MemberException(PASSWORD_NOT_MATCH);
    }

    // 이미 가입한 회원인지 검사
    if (memberRepository.existsByEmail(signUpRequest.getEmail())) {
      throw new MemberException(ALREADY_REGISTERED_USER);
    }

    // 이미지 있다면 s3 저장
    String imageUrl = null;
    if(signUpRequest.getProfileImage() != null){
      imageUrl = s3Service.uploadFile(signUpRequest.getProfileImage(),
          signUpRequest.getEmail());
    }

    // 비밀번호 & 거래 비밀번호 암호화
    signUpRequest.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
    signUpRequest.setTradePassword(passwordEncoder.encode(signUpRequest.getTradePassword()));
    Member member = Member.fromSignUpRequest(signUpRequest, imageUrl);
    memberRepository.save(member);

    return SignUpResponse.fromMember(member);
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
      throw new MemberException(ErrorCode.RESIGNED_ACCOUNT);
    }

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new MemberException(PASSWORD_NOT_MATCH);
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
