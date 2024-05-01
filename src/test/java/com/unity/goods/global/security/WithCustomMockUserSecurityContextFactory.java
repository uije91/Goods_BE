package com.unity.goods.global.security;

import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.global.jwt.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomMockUserSecurityContextFactory implements
    WithSecurityContextFactory<WithCustomMockUser> {

  @Override
  public SecurityContext createSecurityContext(WithCustomMockUser annotation) {

    Member member = Member.builder()
        .nickname(annotation.nickname())
        .email(annotation.email())
        .password(annotation.password())
        .star(annotation.star())
        .role(annotation.role())
        .status(annotation.status())
        .tradePassword(annotation.tradePassword())
        .socialType(annotation.socialType())
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(member);

    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(token);
    return context;
  }
}
