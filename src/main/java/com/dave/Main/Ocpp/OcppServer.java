package com.dave.Main.Ocpp;

import com.dave.Main.Logging.Logger;
import com.dave.Main.State.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class OcppServer implements SmartLifecycle {
    private static final Logger LOGGER = Logger.INSTANCE; // TODO replace with normal logger after implementing the ocpp spec

    private final State state;
    private ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    private boolean isRunning = false;

    @Autowired
    public OcppServer(State state) {
        this.state = state;
    }

    @Override
    public void start() {
        isRunning = true;
        Thread.startVirtualThread(this::runServer);
    }

    private void runServer() {
        try (ServerSocket ws = new ServerSocket(1234)) {
            this.serverSocket = ws;
            LOGGER.print("Waiting for client on port 1234 ...");

            while (!ws.isClosed()) {
                Socket socket = ws.accept();
                this.executorService.execute(new OcppSocketHandler(socket, state));
            }

        } catch (Exception e) {
            if (!isRunning) { // shutdown
                return;
            }
            LOGGER.print(e.getMessage());
            LOGGER.print("Restarting OcppServer ...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException _) {
            }
            this.runServer();
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        this.executorService.shutdown();
        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            } catch (IOException _) {
            }
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

}
