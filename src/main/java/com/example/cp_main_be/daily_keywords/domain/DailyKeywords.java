package com.example.cp_main_be.daily_keywords.domain;

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyKeywords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long id;

    @Column(name = "keyword_date")
    private LocalDateTime keywordDate;

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public void from(AdminRequestDTO.CreateKeywordRequestDTO requestDTO) {
        if(requestDTO.getKeyword() != null && requestDTO.getKeywordDate() != null)
        {
            this.keyword = requestDTO.getKeyword();
            this.keywordDate = requestDTO.getKeywordDate();
        }
        else throw new IllegalStateException("누락된 속성이 존재합니다");
    }
}
