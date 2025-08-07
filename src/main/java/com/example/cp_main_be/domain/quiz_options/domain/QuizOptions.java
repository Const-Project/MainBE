package com.example.cp_main_be.domain.quiz_options.domain;


import com.example.cp_main_be.domain.daily_mission_masters.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizOptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_option_id")
    private Long id;

    @Column(name = "option_text")
    private String optionText;

    @Column(name ="is_correct")
    private boolean isCorrect;

    @Column(name = "option_order")
    private int optionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "mission_master_id")
    private DailyMissionMasters dailyMissionMasters;

}
