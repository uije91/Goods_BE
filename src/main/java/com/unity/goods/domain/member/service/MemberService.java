package com.unity.goods.domain.member.service;

import static com.unity.goods.global.exception.ErrorCode.ALREADY_REGISTERED_USER;
import static com.unity.goods.global.exception.ErrorCode.EMAIL_SEND_ERROR;
import static com.unity.goods.global.exception.ErrorCode.NICKNAME_ALREADY_EXISTS;
import static com.unity.goods.global.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;

import com.unity.goods.domain.email.exception.EmailException;
import com.unity.goods.domain.email.type.EmailSubjects;
import com.unity.goods.domain.member.dto.FindPasswordDto.FindPasswordRequest;
import com.unity.goods.domain.member.dto.LoginDto;
import com.unity.goods.domain.member.dto.ResignDto.ResignRequest;
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
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.infra.service.RedisService;
import com.unity.goods.infra.service.S3Service;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final static String FROM = "Goods";

  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;
  private final RedisService redisService;
  private final S3Service s3Service;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final MailSender mailSender;


  public SignUpResponse signUp(SignUpRequest signUpRequest) {
    // 비밀번호와 비밀번화 확인 일치 검사 (안전성을 위해 프론트에 이어 한번 더 검사)
    if (!signUpRequest.getPassword().equals(signUpRequest.getChkPassword())) {
      throw new MemberException(PASSWORD_NOT_MATCH);
    }

    // 이미 가입한 회원인지 검사
    if (memberRepository.existsByEmail(signUpRequest.getEmail())) {
      throw new MemberException(ALREADY_REGISTERED_USER);
    }

    // nickname 중복 검사
    if (memberRepository.existsByNickname(signUpRequest.getNickName())) {
      throw new MemberException(NICKNAME_ALREADY_EXISTS);
    }

    // 이미지 있다면 s3 저장
    String imageUrl = null;
    if (signUpRequest.getProfileImage() != null) {
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

  // Header 에서 전달받은 "Bearer {AT}" 에서 {AT} 추출
  public String resolveToken(String requestAccessToken) {
    if (requestAccessToken != null && requestAccessToken.startsWith("Bearer ")) {
      return requestAccessToken.substring(7);
    }
    return null;
  }

  // 로그아웃
  @Transactional
  public void logout(String requestAccessToken) {
    String accessToken = resolveToken(requestAccessToken);
    String email = jwtTokenProvider.getAuthentication(accessToken).getName();

    // Redis 에 저장된 RT 삭제
    String redisKey = "RT:" + email;
    String refreshTokenInRedis = redisService.getData(redisKey);
    if (refreshTokenInRedis != null) {
      redisService.deleteData(redisKey);
    }

    // AccessToken 을 BlackList 로 저장(사용방지 처리)
    long expiration = jwtTokenProvider.getTokenExpirationTime(accessToken) - new Date().getTime();
    redisService.setDataExpire(accessToken, "logout", expiration);
  }

  @Transactional
  public void resign(String accessToken, UserDetailsImpl member, ResignRequest resignRequest) {
    Member savedMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    if (!passwordEncoder.matches(resignRequest.getPassword(), savedMember.getPassword())) {
      throw new MemberException(PASSWORD_NOT_MATCH);
    }

    // Redis 에 저장된 RT 삭제
    String redisKey = "RT:" + savedMember.getEmail();
    String refreshTokenInRedis = redisService.getData(redisKey);
    if (refreshTokenInRedis != null) {
      redisService.deleteData(redisKey);
    }

    // AccessToken 을 BlackList 로 저장(사용방지 처리)
    long expiration = jwtTokenProvider.getTokenExpirationTime(accessToken) - new Date().getTime();
    redisService.setDataExpire(accessToken, "resign", expiration);

    savedMember.resignStatus();
  }

  // 비밀번호 찾기 이메일 전송
  @Transactional
  public void findPassword(FindPasswordRequest findPasswordRequest) {

    Member findMember = memberRepository.findByEmail(findPasswordRequest.getEmail())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    String tempPassword = createTempPassword();

    // 임시 비밀번호 전송
    try {
      mailSender.send(createFindPasswordEmail(findMember.getEmail(), tempPassword));
    } catch (MailException e) {
      log.debug("[findPasswordEmail] : 비밀번호 찾기 이메일 전송 과정 중 에러 발생");
      throw new EmailException(EMAIL_SEND_ERROR);
    }

    // db password 임시 비밀번호로 변경
    findMember.changePassword(tempPassword);

  }

  // 비밀번호 찾기 이메일 생성
  public SimpleMailMessage createFindPasswordEmail(String emailAddress,
      String tempPassword) {

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(FROM);
    message.setTo(emailAddress);
    message.setSubject(EmailSubjects.SEND_VERIFICATION_CODE.getTitle());
    message.setText(
        "안녕하세요. 중고거래 마켓 " + FROM + "입니다.\n\n"
            + "임시 비밀번호는 [" + tempPassword + "] 입니다.");

    log.info("[createFindPasswordEmail] : 비밀번호 찾기 이메일 생성 완료");
    return message;
  }

  // 임시 비밀번호 생성
  private static String createTempPassword() {
    final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";

    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 10; i++) {
      sb.append(chars.charAt(random.nextInt(chars.length())));
    }

    log.info("[getTempPassword] : 임시 비밀번호 생성 완료");
    return sb.toString();
  }
}
