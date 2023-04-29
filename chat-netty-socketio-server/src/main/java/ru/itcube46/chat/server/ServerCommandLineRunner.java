package ru.itcube46.chat.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ServerCommandLineRunner implements CommandLineRunner {
    private final SocketIOServer server;

    @Override
    public void run(String... args) throws Exception {
        log.info("Socket.IO server is running");
        server.start();
    }
}
