package com.example.cp_main_be.domain.misson.domain.repository;

import com.example.cp_main_be.domain.misson.domain.DailyMissionMasters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyMissionMastersRepository extends JpaRepository<DailyMissionMasters, Long> {

}
