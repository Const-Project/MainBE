package com.example.cp_main_be.domain.notification.domain.repository;

import com.example.cp_main_be.domain.notification.domain.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}
