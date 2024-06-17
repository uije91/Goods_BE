package com.unity.goods.domain.notification.repository;

import com.unity.goods.domain.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationLog, Long> {

}
