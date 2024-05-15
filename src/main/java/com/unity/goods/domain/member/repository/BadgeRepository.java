package com.unity.goods.domain.member.repository;

import com.unity.goods.domain.member.entity.Badge;
import com.unity.goods.domain.member.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
  List<Badge> findAllByMember(Member member);
}
