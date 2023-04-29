module ru.itcube46.chat.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;

    requires socket.io.client;
    requires engine.io.client;
    requires okhttp3;
    requires json;

    opens ru.itcube46.chat.client to javafx.fxml;
    exports ru.itcube46.chat.client;
}
