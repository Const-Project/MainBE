package com.example.cp_main_be.domain.delivery.domain.repository;

import com.example.cp_main_be.domain.delivery.domain.Delivery;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
  List<Delivery> findByUserId(Long userId);

  @Modifying
  @Query("DELETE FROM Delivery d WHERE d.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
