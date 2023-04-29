package ru.itcube46.chat.server.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import ru.itcube46.chat.server.entities.Question;

@Repository
public class QuestionsRepository {
    private Map<String, List<Question>> questionsMap = new HashMap<>();

    public QuestionsRepository() {
        questionsMap.put("GeoQuiz", List.of(
                new Question("Какой город является столицей Италии?",
                        List.of("Рим", "Милан", "Венеция"), "1"),
                new Question("В какой стране находится город Мачу-Пикчу?",
                        List.of("Португалия", "Испания", "Перу"), "3"),
                new Question("Как называется самый высокий водопад в мире?",
                        List.of("Виктория", "Ниагарский", "Игуасу"), "2")));
    }

    public List<Question> findAllByQuizName(String name) {
        return questionsMap.get(name);
    }
}
