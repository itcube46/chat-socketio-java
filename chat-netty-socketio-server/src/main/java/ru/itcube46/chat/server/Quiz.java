package ru.itcube46.chat.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import ru.itcube46.chat.server.entities.Question;
import ru.itcube46.chat.server.repositories.QuestionsRepository;

@RequiredArgsConstructor
public class Quiz {
    private final QuestionsRepository questionsRepository;
    private List<Player> players = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();
    private int questionIndex = -1;

    public boolean init(String name) {
        List<Question> foundQuestions = questionsRepository.findAllByQuizName(name);
        if (foundQuestions != null) {
            questions = foundQuestions;
            nextQuestion();
            return true;
        }
        return false;
    }

    public boolean addPlayer(String username) {
        if (findPlayerByName(username).isEmpty()) {
            var player = new Player();
            player.setName(username);
            players.add(player);
            return true;
        }
        return false;
    }

    public boolean checkAnswer(String answer, String username) {
        Player player = findPlayerByName(username).get();
        if (questions.get(questionIndex).getAnswer().equalsIgnoreCase(answer)) {
            player.increaseScore();
            return true;
        }
        player.decreaseScore();
        return false;
    }

    public void nextQuestion() {
        questionIndex++;
    }

    public boolean isOver() {
        return questionIndex >= questions.size();
    }

    public Optional<Player> findPlayerByName(String name) {
        return players.stream().filter(p -> p.name.equals(name)).findAny();
    }

    public String getLeader() {
        return players.stream().max(Comparator.comparing(Player::getScore)).get().getName();
    }

    public void reward() {
        // Вознаграждение победителя
    }

    public String printQuestion() {
        Question currentQuestion = questions.get(questionIndex);
        int variantIndex = 1;
        StringBuilder str = new StringBuilder(currentQuestion.getText());
        for (String variant : currentQuestion.getVariants()) {
            str.append("\n").append(variantIndex).append(": ").append(variant);
            variantIndex++;
        }
        return str.toString();
    }

    public static class Player {
        private String name;
        private int score;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void increaseScore() {
            score++;
        }

        public void decreaseScore() {
            score--;
        }

        public int getScore() {
            return score;
        }
    }
}
