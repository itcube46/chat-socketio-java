package ru.itcube46.chat.server;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import lombok.extern.slf4j.Slf4j;
import ru.itcube46.chat.server.repositories.QuestionsRepository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Chat {
    private final MessageService msgService;
    private final QuestionsRepository questionsRepository;
    private final ConcurrentMap<String, Quiz> quizStates = new ConcurrentHashMap<>();

    public Chat(SocketIOServer server, MessageService msgService, QuestionsRepository questionsRepository) {
        this.msgService = msgService;
        this.questionsRepository = questionsRepository;
        server.addConnectListener(onConnect());
        server.addDisconnectListener(onDisconnect());
        server.addEventListener("join_room", ChatMessage.class, onJoinRoom());
        server.addEventListener("message", ChatMessage.class, onMessage());
    }

    private ConnectListener onConnect() {
        return client -> {
            log.info("Client [{}] connected", client.getSessionId().toString());
        };
    }

    private DisconnectListener onDisconnect() {
        return client -> {
            log.info("Client [{}] disconnected", client.getSessionId().toString());
        };
    }

    private DataListener<ChatMessage> onJoinRoom() {
        return (client, data, ackSender) -> {
            var user = data.getUser();
            var room = data.getRoom();
            client.joinRoom(room);
            msgService.sendJoinMessage(client, user, room);
            log.info("Client [{}] joined to {}", client.getSessionId().toString(), room);
        };
    }

    private DataListener<ChatMessage> onMessage() {
        return (senderClient, data, ackSender) -> {
            var user = data.getUser();
            var room = data.getRoom();
            var msg = data.getMessage().trim();
            var quiz = quizStates.get(room);

            if (quiz == null && msg.startsWith("/q ")) {
                quiz = new Quiz(questionsRepository);
                if (quiz.init(msg.substring(3))) {
                    quiz.addPlayer(user);
                    quizStates.put(room, quiz);
                    msgService.sendQuizMessage(senderClient, room, quiz.printQuestion());
                } else {
                    msgService.sendQuizMessage(senderClient, room, "Викторины с таким названием нет");
                }
            } else if (quiz != null && msg.startsWith("/q ")) {
                msgService.sendQuizMessage(senderClient, room,
                        "Новую викторину нельзя запустить, пока старая не завершена");
            } else if (quiz != null && msg.startsWith("/a ")) {
                quiz.addPlayer(user);
                if (quiz.checkAnswer(msg.substring(3), user)) {
                    msgService.sendQuizMessage(senderClient, room, user + " ответил верно!");
                    quiz.nextQuestion();
                    if (quiz.isOver()) {
                        msgService.sendQuizMessage(senderClient, room,
                                "Викторина завершена!\n" + "Победу одерживает " + quiz.getLeader());
                        quizStates.remove(room); // Удаляем викторину
                    } else {
                        msgService.sendQuizMessage(senderClient, room, quiz.printQuestion());
                    }
                } else {
                    msgService.sendQuizMessage(senderClient, room, user + " ошибся");
                }
            } else if (quiz == null && msg.startsWith("/a ")) {
                msgService.sendQuizMessage(senderClient, room, "Викторина не запущена");
            } else {
                msgService.sendMessage(senderClient, user, room, msg);
            }
        };
    }
}
