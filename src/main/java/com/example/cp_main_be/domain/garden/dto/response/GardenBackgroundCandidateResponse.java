package com.example.cp_main_be.domain.garden.dto.response;

import com.example.cp_main_be.domain.garden.domain.GardenBackground;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GardenBackgroundCandidateResponse {
  private final Long id;
  private final String name;
  private final String imageUrl;

  public static GardenBackgroundCandidateResponse from(GardenBackground gardenBackground) {
    return GardenBackgroundCandidateResponse.builder()
        .id(gardenBackground.getId())
        .name(gardenBackground.getName())
        .imageUrl(gardenBackground.getImageUrl())
        .build();
  }
}
