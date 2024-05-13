package com.unity.goods.domain.member.service;

import static com.unity.goods.domain.member.type.SocialType.SERVER;
import static com.unity.goods.domain.member.type.Status.INACTIVE;
import static com.unity.goods.domain.member.type.Status.RESIGN;
import static com.unity.goods.global.exception.ErrorCode.ALREADY_REGISTERED_USER;
import static com.unity.goods.global.exception.ErrorCode.CURRENT_USED_PASSWORD;
import static com.unity.goods.global.exception.ErrorCode.EMAIL_NOT_VERITY;
import static com.unity.goods.global.exception.ErrorCode.EMAIL_SEND_ERROR;
import static com.unity.goods.global.exception.ErrorCode.INVALID_REFRESH_TOKEN;
import static com.unity.goods.global.exception.ErrorCode.NICKNAME_ALREADY_EXISTS;
import static com.unity.goods.global.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static com.unity.goods.global.exception.ErrorCode.RESIGNED_ACCOUNT;
import static com.unity.goods.global.exception.ErrorCode.USER_NOT_FOUND;
import static com.unity.goods.global.exception.ErrorCode.USE_SOCIAL_LOGIN;

import com.unity.goods.domain.email.exception.EmailException;
import com.unity.goods.domain.email.type.EmailSubjects;
import com.unity.goods.domain.member.dto.ChangePasswordDto.ChangePasswordRequest;
import com.unity.goods.domain.member.dto.ChangeTradePasswordDto.ChangeTradePasswordRequest;
import com.unity.goods.domain.member.dto.FindPasswordDto.FindPasswordRequest;
import com.unity.goods.domain.member.dto.LoginDto;
import com.unity.goods.domain.member.dto.MemberProfileDto.MemberProfileResponse;
import com.unity.goods.domain.member.dto.ResignDto.ResignRequest;
import com.unity.goods.domain.member.dto.SignUpDto.SignUpRequest;
import com.unity.goods.domain.member.dto.SignUpDto.SignUpResponse;
import com.unity.goods.domain.member.dto.UpdateProfileDto.UpdateProfileRequest;
import com.unity.goods.domain.member.dto.UpdateProfileDto.UpdateProfileResponse;
import com.unity.goods.domain.member.entity.Badge;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.exception.MemberException;
import com.unity.goods.domain.member.repository.BadgeRepository;
import com.unity.goods.domain.member.repository.MemberRepository;
import com.unity.goods.domain.member.type.BadgeType;
import com.unity.goods.domain.model.TokenDto;
import com.unity.goods.global.jwt.JwtTokenProvider;
import com.unity.goods.global.jwt.UserDetailsImpl;
import com.unity.goods.infra.service.RedisService;
import com.unity.goods.infra.service.S3Service;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
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
  private final BadgeRepository badgeRepository;
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
          signUpRequest.getEmail() + "/" + "profileImage");
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
      throw new MemberException(USER_NOT_FOUND);
    }

    Member member = optionalMember.get();
    if (member.getSocialType() != SERVER) {
      throw new MemberException(USE_SOCIAL_LOGIN);
    }

    if (member.getStatus() == INACTIVE) {
      throw new MemberException(EMAIL_NOT_VERITY);
    }

    if (member.getStatus() == RESIGN) {
      throw new MemberException(RESIGNED_ACCOUNT);
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
    long expiration =
        jwtTokenProvider.getTokenExpirationTime(tokenDto.getRefreshToken()) - new Date().getTime();

    redisService.setDataExpire("RT:" + email, tokenDto.getRefreshToken(), expiration);
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
      log.error("[MemberService][findPasswordEmail] : 비밀번호 찾기 이메일 전송 과정 중 에러 발생");
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

    log.info("[MemberService][createFindPasswordEmail] : 비밀번호 찾기 이메일 생성 완료. 수신인 : {}",
        emailAddress);
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

    log.info("[MemberService][getTempPassword] : 임시 비밀번호 생성 완료");
    return sb.toString();
  }

  // 토큰 재발급
  public String reissue(TokenDto tokenDto) {
    // 1. RT 검증
    if (!jwtTokenProvider.validateToken(tokenDto.getRefreshToken())) {
      log.error("[MemberService][reissue] : RefreshToken 검증 실패");
      throw new MemberException(INVALID_REFRESH_TOKEN);
    }

    // 2.AT 에서 email 정보 습득
    String accessToken = resolveToken(tokenDto.getAccessToken());
    String email = (String) jwtTokenProvider.getClaims(accessToken).get("email");
    String role = (String) jwtTokenProvider.getClaims(accessToken).get("role");

    // 3.Redis 에 저장된 RT와 가져온 RT 비교
    String refreshToken = redisService.getData("RT:" + email);
    if (!refreshToken.equals(tokenDto.getRefreshToken())) {
      log.error("[MemberService][reissue] : Redis에 저장된 RT와 가져온 RT가 불일치");
      throw new MemberException(INVALID_REFRESH_TOKEN);
    }

    // 4.새로운 토큰 생성 및 반환
    return jwtTokenProvider.generateAccessToken(email, role);
  }

  // 회원 프로필 조회
  public MemberProfileResponse getMemberProfile(UserDetailsImpl member) {

    Member findMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    if (findMember.getStatus() == RESIGN) {
      throw new MemberException(RESIGNED_ACCOUNT);
    }

    return MemberProfileResponse.fromMember(findMember);
  }

  // 회원 프로필 수정
  @Transactional
  public UpdateProfileResponse updateMemberProfile(UserDetailsImpl member,
      UpdateProfileRequest updateProfileRequest) {

    Member findMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    // 변경 사항 있는 필드만 프로필 변경
    if (updateProfileRequest.getNickName() != null) {
      findMember.setNickname(updateProfileRequest.getNickName());
    }

    if (updateProfileRequest.getPhoneNumber() != null) {
      findMember.setPhoneNumber(updateProfileRequest.getPhoneNumber());
    }

    if (updateProfileRequest.getProfileImage() != null) {
      // 기존에 프로필 이미지가 있다면 S3에서 파일 삭제
      if (findMember.getProfileImage() != null) {
        s3Service.deleteFile(findMember.getProfileImage());
      }
      findMember.setProfileImage(
          s3Service.uploadFile(
              updateProfileRequest.getProfileImage(), member.getUsername()));
    }

    return UpdateProfileResponse.fromMember(findMember);
  }

  // 비밀번호 변경
  @Transactional
  public void changePassword(ChangePasswordRequest changePasswordRequest,
      UserDetailsImpl member) {
    Member findMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    if (!passwordEncoder.matches(changePasswordRequest.getCurPassword(), member.getPassword())) {
      throw new MemberException(PASSWORD_NOT_MATCH);
    }

    if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), member.getPassword())) {
      throw new MemberException(CURRENT_USED_PASSWORD);
    }

    findMember.changePassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
  }

  // 거래 비밀번호 변경
  @Transactional
  public void changeTradePassword(ChangeTradePasswordRequest changeTradePasswordRequest,
      UserDetailsImpl member) {
    Member findMember = memberRepository.findByEmail(member.getUsername())
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    if (!passwordEncoder.matches(changeTradePasswordRequest.getCurTradePassword(),
        findMember.getTradePassword())) {
      throw new MemberException(PASSWORD_NOT_MATCH);
    }

    if (passwordEncoder.matches(changeTradePasswordRequest.getNewTradePassword(),
        findMember.getTradePassword())) {
      throw new MemberException(CURRENT_USED_PASSWORD);
    }

    findMember.setTradePassword(
        passwordEncoder.encode(changeTradePasswordRequest.getNewTradePassword()));
  }

  public List<BadgeType> getBadge(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new MemberException(USER_NOT_FOUND));

    List<Badge> badgeList = badgeRepository.findAllByMember_Id(member.getId());

    return badgeList.stream().map(Badge::getBadge).toList();
  }
}
