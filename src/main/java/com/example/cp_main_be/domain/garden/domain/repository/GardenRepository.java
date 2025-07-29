package com.example.cp_main_be.domain.garden.domain.repository;

import com.example.cp_main_be.domain.garden.domain.Garden;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GardenRepository extends JpaRepository<Garden, Long> {
  Optional<Garden> findById(Long id);
}
