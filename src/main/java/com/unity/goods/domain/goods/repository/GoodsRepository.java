package com.unity.goods.domain.goods.repository;

import com.unity.goods.domain.goods.entity.Goods;
import com.unity.goods.domain.goods.type.GoodsStatus;
import com.unity.goods.domain.member.entity.Member;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {

  Page<Goods> findByMemberId(Long memberId, Pageable pageable);

  List<Goods> findAllByMemberAndGoodsStatus(Member member, GoodsStatus goodsStatus);
}
