package ru.itcube46.chat.server;

import com.corundumstudio.socketio.SocketIOClient;

import org.springframework.stereotype.Service;

@Service
public class MessageService {
    public void sendMessage(SocketIOClient senderClient, String user, String room, String message) {
        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            client.sendEvent("message", new ChatMessage(user, room, message));
        }
    }

    public void sendQuizMessage(SocketIOClient senderClient, String room, String message) {
        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            client.sendEvent("quiz_message", new ChatMessage("QUIZ", room, message));
        }
    }

    public void sendJoinMessage(SocketIOClient socketClient, String user, String room) {
        for (SocketIOClient client : socketClient.getNamespace().getRoomOperations(room).getClients()) {
            var msg = new ChatMessage("INFO", room, "Пользователь '%s' присоединился к чату".formatted(user));
            client.sendEvent("join_room", msg);
        }
    }
}
