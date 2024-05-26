package com.unity.goods.domain.chat.repository;

import com.unity.goods.domain.chat.entity.ChatRoom;
import com.unity.goods.domain.goods.entity.Goods;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  List<ChatRoom> findAllByBuyerIdOrSellerId(Long buyerId, Long sellerId);

  Optional<ChatRoom> findByBuyerIdAndSellerIdAndGoods(Long buyerId, Long sellerId, Goods goods);

  @Query("SELECT CASE WHEN c.sellerId = :memberId THEN c.buyerId WHEN c.buyerId = :memberId THEN c.sellerId ELSE null END " +
      "FROM ChatRoom c WHERE (c.sellerId = :memberId OR c.buyerId = :memberId) AND c.id IS NOT NULL AND c.sellerId IS NOT NULL AND c.buyerId IS NOT NULL")
  Long findOppositeMemberIdByMemberId(Long memberId);
}
