package com.unity.goods.domain.member.controller;

import com.unity.goods.domain.member.dto.ChangePasswordDto.ChangePasswordRequest;
import com.unity.goods.domain.member.dto.SignUpRequest;
import com.unity.goods.domain.member.dto.SignUpResponse;
import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

  private final MemberService memberService;

  @PostMapping("/signup")
  public ResponseEntity<?> signUpMember(
      @RequestBody SignUpRequest signUpRequest) {
    SignUpResponse signUpResponse = memberService.signUpMember(signUpRequest);
    return ResponseEntity.ok(signUpResponse);
  }

  @PutMapping("/change")
  public ResponseEntity<?> changePassword(
      @RequestBody @Valid ChangePasswordRequest changePasswordRequest,
      @AuthenticationPrincipal Member member) {
    memberService.changePassword(changePasswordRequest, member);
    return ResponseEntity.ok().build();
  }

}
