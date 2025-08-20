package com.example.cp_main_be.domain.plant_masters.domain.repository;

import com.example.cp_main_be.domain.garden.domain.Garden;
import com.example.cp_main_be.domain.plant_masters.domain.PlantMasters;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantMasterRepository extends JpaRepository<PlantMasters, Long> {
}
