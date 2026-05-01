package com.example.cp_main_be.domain.member.notification.domain.repository;

import com.example.cp_main_be.domain.member.notification.domain.Notification;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId);

  int countByReceiverAndIsReadFalse(User user);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE Notification n SET n.isRead = true "
          + "WHERE n.receiver.id = :userId AND n.isRead = false")
  int markAllAsReadByReceiverId(@Param("userId") Long userId);

  @Modifying
  @Query("DELETE FROM Notification n WHERE n.receiver = :user")
  void deleteAllByReceiver(@Param("user") User user);
}
