package com.unity.goods.domain.chat.repository;

import com.unity.goods.domain.chat.entity.ChatRoom;
import com.unity.goods.domain.goods.entity.Goods;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  List<ChatRoom> findAllByBuyerIdOrSellerId(Long buyerId, Long sellerId);

  Optional<ChatRoom> findByBuyerIdAndSellerIdAndGoods(Long buyerId, Long sellerId, Goods goods);
}
