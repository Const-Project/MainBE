package com.example.cp_main_be.domain.quiz.domain;

import com.example.cp_main_be.domain.daily_mission_masters.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.quiz.enums.QuizType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_question")
    private String quizQuestion;

    @Column(name = "quiz_type")
    private QuizType quizType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_master_id")
    private DailyMissionMasters dailyMissionMasters;

}
