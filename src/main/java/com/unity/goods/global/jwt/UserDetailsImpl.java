package com.unity.goods.global.jwt;

import com.unity.goods.domain.member.entity.Member;
import java.util.ArrayList;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

  private final Member member;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(member.getRole().toString()));
    return authorities;
  }

  @Override
  public String getUsername() {
    return member.getEmail();
  }

  @Override
  public String getPassword() {
    return member.getPassword();
  }

  @Override
  public boolean isAccountNonExpired() { // 계정의 만료 여부
    return true;
  }

  @Override
  public boolean isAccountNonLocked() { // 계정의 잠김 여부
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() { // 비밀번호 만료 여부
    return true;
  }

  @Override
  public boolean isEnabled() { // 계정의 활성화 여부
    return true;
  }
}
