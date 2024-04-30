package com.unity.goods.global.jwt;

import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Member findMember = memberRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("이메일 정보가 없습니다."));

    if (findMember != null) {
      UserDetailsImpl userDetails = new UserDetailsImpl(findMember);
      return userDetails;
    }
    return null;
  }
}
