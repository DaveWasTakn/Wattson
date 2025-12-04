package com.dave;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static void main() {

        try (ServerSocket ws = new ServerSocket(1234)) {
            System.out.println("Waiting for client on port 1234 ...");

            try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {

                while (!ws.isClosed()) {
                    Socket socket = ws.accept();
                    executorService.execute(new OcppHandler(socket));
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
