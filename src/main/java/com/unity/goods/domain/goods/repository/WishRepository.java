package com.unity.goods.domain.goods.repository;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.entity.Wishlist;
import com.unity.goods.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishRepository extends JpaRepository<Wishlist, Long> {

  boolean existsByGoodsAndMember(Goods goods, Member member);

  Optional<Wishlist> findByGoodsAndMember(Goods goods, Member member);

  Page<Wishlist> findByMemberId(Long memberId, Pageable pageable);
}
