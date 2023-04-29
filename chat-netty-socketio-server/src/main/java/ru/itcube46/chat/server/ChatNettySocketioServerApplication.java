package ru.itcube46.chat.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

@SpringBootApplication
public class ChatNettySocketioServerApplication {
    @Bean
    public SocketIOServer socketioServer() {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);
        return new SocketIOServer(config);
    }

    public static void main(String[] args) {
        SpringApplication.run(ChatNettySocketioServerApplication.class, args);
    }
}
