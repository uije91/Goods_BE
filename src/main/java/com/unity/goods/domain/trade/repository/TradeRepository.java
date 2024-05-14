package com.unity.goods.domain.trade.repository;

import com.unity.goods.domain.trade.entity.Trade;
import com.unity.goods.domain.trade.type.TradePurpose;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

  @EntityGraph(attributePaths = {"goods", "goods.member"})
  Page<Trade> findByMemberIdAndTradePurpose(Long memberId, TradePurpose tradePurpose,
      Pageable pageable);
}
