package com.unity.goods.domain.chat.repository;

import com.unity.goods.domain.chat.entity.ChatRoom;
import com.unity.goods.domain.goods.entity.Goods;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  Page<ChatRoom> findAllByBuyerIdOrSellerId(Long buyerId, Long sellerId, Pageable pageable);

  Optional<ChatRoom> findByBuyerIdAndSellerIdAndGoods(Long buyerId, Long sellerId, Goods goods);

  @Query("SELECT CASE WHEN c.buyerId = :memberId THEN c.sellerId WHEN c.sellerId = :memberId THEN c.buyerId ELSE NULL END " +
      "FROM ChatRoom c WHERE c.id = :chatRoomId AND (:memberId = c.buyerId OR :memberId = c.sellerId)")
  Long findOppositeMemberId(Long chatRoomId, Long memberId);
}
