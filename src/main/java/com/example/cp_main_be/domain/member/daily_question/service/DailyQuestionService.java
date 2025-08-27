package com.example.cp_main_be.domain.member.daily_question.service;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class DailyQuestionService {
  private static final List<String> QUESTIONS =
      List.of(
          "어제 하루는 즐거우셨나요?",
          "오늘 아침은 챙겨 드셨나요?",
          "오늘 아침, 잠자리에서 일어날 때 몸이 가뿐하게 느껴지셨나요?",
          "오늘 하루, 무언가 해보고 싶다는 생각이 드시나요?",
          "요즘 하루하루를 보내는 것이 보람 있다고 느끼시나요?",
          "오늘 하루, 무언가 기대되거나 즐거운 일이 있을 것 같다는 생각이 드시나요?",
          "요즘 소소하게라도 웃거나 미소 짓는 일이 자주 있으신가요?",
          "요즘 마음이 가라앉거나 울적한 날 없이, 편안하게 지내고 계신가요?",
          "특별히 걱정되는 일 없이, 마음이 편안하고 안정된 상태이신가요?",
          "요즘 밖에 나가 산책을 하거나 사람들을 만나는 것이 편안하게 느껴지시나요?",
          "물건을 둔 곳이나 하려던 일들을 선명하게 잘 기억하고 계신가요?",
          "가까운 사람들의 이름이나 약속 같은 것들이 예전처럼 잘 떠오르시나요?",
          "평소 하던 집안일이나 가벼운 활동을 하기에 기운이 충분하다고 느껴지시나요?",
          "거울에 비친 내 모습이 괜찮아 보이시나요?",
          "최근 새롭게 시작하고 싶은 취미들을 생각하신 적이 있으실까요?",
          "요즘 마음이 가는 취미나 재미있는 활동이 있으신가요?",
          "저희 식물을 기르며 즐거움을 얻으셨나요?",
          "어젯밤, 편안하게 푹 주무셨나요?",
          "주무시는 동안 중간에 깨지 않고 깊이 잠드는 날이 많으신가요?",
          "요즘 입맛이 좋고, 음식이 맛있게 느껴지시나요?",
          "평소와 같은 일상적인 일들을 꾸준히 하고 계실까요?");

  @Getter
  @RequiredArgsConstructor
  public static class QuestionInfo {
    private final Long id;
    private final String text;
  }

  public QuestionInfo getQuestionInfoForToday() {
    int dayOfYear = LocalDate.now().getDayOfYear();
    int questionIndex = (dayOfYear - 1) % QUESTIONS.size();
    String question = QUESTIONS.get(questionIndex);
    return new QuestionInfo((long) questionIndex, question);
  }

  public String getQuestionById(Long id) {
    return QUESTIONS.get(id.intValue());
  }
}
