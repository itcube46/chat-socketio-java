package ru.itcube46.chat.client;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ChatController {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextFlow chatFlow;
    @FXML
    private TextField nameField;
    @FXML
    private TextField roomField;
    @FXML
    private TextField messageField;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private Button sendButton;

    private String user;
    private String room;
    private Socket socket;
    private final Dispatcher dispatcher;

    {
        dispatcher = new Dispatcher();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        IO.Options options = new IO.Options();
        options.callFactory = okHttpClient;
        options.webSocketFactory = okHttpClient;

        try {
            socket = IO.socket("http://localhost:9092", options);
            socket.on(Socket.EVENT_CONNECT, onConnect());
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError());
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect());
            socket.on("join_room", onJoinRoom());
            socket.on("message", onMessage());
            socket.on("quiz_message", onQuizMessage());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        sendButton.setDisable(true);
        chatFlow.heightProperty().addListener(observable -> scrollPane.setVvalue(1.0));
    }

    @FXML
    public void connect() {
        connectButton.setDisable(true);
        disconnectButton.setDisable(false);
        sendButton.setDisable(false);
        user = nameField.getText();
        room = roomField.getText();

        socket = socket.connect();
    }

    @FXML
    public void disconnect() {
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        sendButton.setDisable(true);

        socket = socket.disconnect();
    }

    @FXML
    public void send() {
        var object = createMessageObject(user, room, messageField.getText());
        socket.emit("message", object);
    }

    private Emitter.Listener onConnect() {
        return objects -> {
            var logMsg = "User '%s' [%s] connected".formatted(user, socket.id());
            System.out.println(logMsg);
            socket.emit("join_room", createMessageObject(user, room, ""));
        };
    }

    private Emitter.Listener onConnectError() {
        return objects -> {
            var logMsg = "Connection error: %s".formatted(Arrays.toString(objects));
            System.err.println(logMsg);
        };
    }

    private Emitter.Listener onDisconnect() {
        return objects -> {
            var logMsg = "User '%s' disconnected: %s".formatted(user, Arrays.toString(objects));
            System.out.println(logMsg);
        };
    }

    private Emitter.Listener onJoinRoom() {
        return objects -> {
            var object = (JSONObject) objects[0];
            try {
                var userText = new Text(object.getString("user") + ": ");
                var messageText = new Text(object.getString("message") + '\n');
                userText.getStyleClass().addAll("b", "text-success");
                messageText.getStyleClass().addAll("p", "text-info");
                Platform.runLater(() -> chatFlow.getChildren().addAll(userText, messageText));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Emitter.Listener onMessage() {
        return objects -> {
            var object = (JSONObject) objects[0];
            try {
                var userText = new Text(object.getString("user") + ": ");
                var messageText = new Text(object.getString("message") + '\n');
                userText.getStyleClass().addAll("b", "text-primary");
                messageText.getStyleClass().addAll("p");
                Platform.runLater(() -> chatFlow.getChildren().addAll(userText, messageText));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Emitter.Listener onQuizMessage() {
        return objects -> {
            var object = (JSONObject) objects[0];
            try {
                var userText = new Text(object.getString("user") + ": ");
                var messageText = new Text(object.getString("message") + '\n');
                userText.getStyleClass().addAll("b", "text-danger");
                messageText.getStyleClass().addAll("p", "text-warning");
                Platform.runLater(() -> chatFlow.getChildren().addAll(userText, messageText));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private JSONObject createMessageObject(String user, String room, String message) {
        JSONObject object = new JSONObject();
        try {
            object.put("user", user);
            object.put("room", room);
            object.put("message", message);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return object;
    }

    public void close() {
        if (socket.connected()) {
            socket.disconnect();
        }
        dispatcher.executorService().shutdown();
    }
}
