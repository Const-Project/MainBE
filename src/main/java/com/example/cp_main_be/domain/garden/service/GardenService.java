package com.example.cp_main_be.domain.garden.service;

import com.example.cp_main_be.domain.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.garden.dto.GardenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GardenService {

  private final GardenRepository gardenRepository;

  public GardenResponse findGardenById(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("Garden not found"));

    return GardenResponse.from(garden);
  }

  @Transactional
  public void waterGarden(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("해당 텃밭을 찾을 수 없습니다."));

    garden.increaseWaterCount();
  }

  @Transactional
  public void sunlightGarden(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("해당 텃밭을 찾을 수 없습니다."));

    garden.increaseSunlightCount();
  }
}
