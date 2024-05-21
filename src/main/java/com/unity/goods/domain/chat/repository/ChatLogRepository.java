package com.unity.goods.domain.chat.repository;

import com.unity.goods.domain.chat.entity.ChatLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {

  List<ChatLog> findAllByChatRoomIdAndCheckedAndReceiver(
      Long chatRoomId, boolean checked, String receiver);

  int countAllByReceiverAndChecked(String receiver, boolean checked);

}
