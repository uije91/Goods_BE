package com.unity.goods.global.security;

import static com.unity.goods.domain.member.type.Role.USER;
import static com.unity.goods.domain.member.type.SocialType.SERVER;
import static com.unity.goods.domain.member.type.Status.INACTIVE;

import com.unity.goods.domain.member.type.Role;
import com.unity.goods.domain.member.type.SocialType;
import com.unity.goods.domain.member.type.Status;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {

  String nickname() default "test";

  String email() default "email@test.com";

  String password() default "test123";

  double star() default 0.0;

  Role role() default USER;

  Status status() default INACTIVE;

  String tradePassword() default "1234";

  SocialType socialType() default SERVER;


}