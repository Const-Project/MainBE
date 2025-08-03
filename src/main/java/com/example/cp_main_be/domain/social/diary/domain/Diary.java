package com.example.cp_main_be.domain.social.diary.domain;

import com.example.cp_main_be.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "diaries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob // TEXT 타입을 위해 사용
    @Column(nullable = false)
    private String content;

    @Temporal(TemporalType.DATE)
    @Column(name = "diary_date")
    private Date diaryDate;

    private String keyword;

    private String imageUrl;

    @Column(name = "is_public")
    private boolean isPublic = true; // 기본값 true

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.likeCount == null) {
            this.likeCount = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}