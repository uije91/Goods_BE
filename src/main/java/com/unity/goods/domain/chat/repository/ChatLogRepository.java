package com.unity.goods.domain.chat.repository;

import com.unity.goods.domain.chat.entity.ChatLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {

  List<ChatLog> findAllByChatRoomIdAndCheckedAndReceiverId(
      Long chatRoomId, boolean checked, Long receiverId);

  int countAllByReceiverIdAndChecked(Long receiverId, boolean checked);

}
