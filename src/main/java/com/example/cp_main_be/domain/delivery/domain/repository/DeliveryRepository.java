package com.example.cp_main_be.domain.delivery.domain.repository;

import com.example.cp_main_be.domain.delivery.domain.Delivery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
  List<Delivery> findByUserId(Long userId);
}
