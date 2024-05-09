package com.unity.goods.domain.member.repository;

import com.unity.goods.domain.member.entity.Member;
import com.unity.goods.domain.member.type.Status;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  Member findMemberByEmail(String email);
}
