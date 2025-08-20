package com.example.cp_main_be.domain.garden.plant_masters.domain;

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.admin.dto.AdminResponseDTO;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlantMasters {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "plant_master_id")
  private Long id;

  @Column(name = "plant_name")
  private String plantName;

  @Column(name = "plant_type")
  private String plantType;

  @Column(name = "description")
  private String description;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "growth_stages")
  private int growthStages = 0;

  @Column(name = "unlock_level")
  private int unlockLevel = 0;

  @Column(name = "created_at")
  @CreationTimestamp
  private LocalDateTime createdAt;

  public void update(AdminRequestDTO.UpdatePlantMasterRequestDTO requestDTO) {
    if (requestDTO.getPlantName() != null) {
      this.plantName = requestDTO.getPlantName();
    }
    if (requestDTO.getPlantType() != null) {
      this.plantType = requestDTO.getPlantType();
    }
    if (requestDTO.getDescription() != null) {
      this.description = requestDTO.getDescription();
    }
    if (requestDTO.getImageUrl() != null) {
      this.imageUrl = requestDTO.getImageUrl();
    }
  }

  public static AdminResponseDTO.PlantMasterResDTO toPlantMasterResDTO(PlantMasters plantMasters) {
    return AdminResponseDTO.PlantMasterResDTO.builder()
        .id(plantMasters.getId())
        .plantName(plantMasters.getPlantName())
        .plantType(plantMasters.getPlantType())
        .description(plantMasters.getDescription())
        .imageUrl(plantMasters.getImageUrl())
        .growthStages(plantMasters.getGrowthStages())
        .unlockLevel(plantMasters.getUnlockLevel())
        .createdAt(plantMasters.getCreatedAt())
        .build();
  }
}
