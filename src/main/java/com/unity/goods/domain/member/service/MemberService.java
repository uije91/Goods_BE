package com.unity.goods.domain.member.service;

import static com.unity.goods.global.exception.ErrorCode.CURRENT_USED_PASSWORD;
import static com.unity.goods.global.exception.ErrorCode.EMAIL_SEND_ERROR;
import static com.unity.goods.global.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.unity.goods.domain.email.exception.EmailException;
import com.unity.goods.domain.email.type.EmailSubjects;
import com.unity.goods.domain.member.dto.ChangePasswordDto.ChangePasswordRequest;
import com.unity.goods.domain.member.dto.FindPasswordDto.FindPasswordRequest;
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
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.global.service.RedisService;
import java.security.SecureRandom;
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
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final MailSender mailSender;


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
      throw new MemberException(ErrorCode.RESIGNED_ACCOUNT);
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

  @Transactional
  public void changePassword(ChangePasswordRequest changePasswordRequest,
      UserDetailsImpl member) {
    Member findMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));

    if (passwordEncoder.matches(changePasswordRequest.getPassword(), member.getPassword())) {
      throw new MemberException(CURRENT_USED_PASSWORD);
    }
    findMember.changePassword(passwordEncoder.encode(changePasswordRequest.getPassword()));

  }

  // 비밀번호 찾기 이메일 전송
  @Transactional
  public void findPassword(FindPasswordRequest findPasswordRequest) {

    Member findMember = memberRepository.findByEmail(findPasswordRequest.getEmail())
        .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));

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

    for(int i=0; i<10; i++) {
      sb.append(chars.charAt(random.nextInt(chars.length())));
    }

    log.info("[getTempPassword] : 임시 비밀번호 생성 완료");
    return sb.toString();
  }
}
