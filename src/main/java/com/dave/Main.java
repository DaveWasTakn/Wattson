package com.dave;

import com.dave.Logging.Logger;
import com.dave.Ocpp.OcppSocketHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final Logger LOGGER = Logger.INSTANCE;

    static void main() {

        try (ServerSocket ws = new ServerSocket(1234)) {
            LOGGER.print("Waiting for client on port 1234 ...");

            try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

                while (!ws.isClosed()) {
                    Socket socket = ws.accept();
                    executorService.execute(new OcppSocketHandler(socket));
                }

            }

        } catch (Exception e) {
            LOGGER.print(e.getMessage());
            LOGGER.print("Restarting ...");
            main();
        }

    }
}
