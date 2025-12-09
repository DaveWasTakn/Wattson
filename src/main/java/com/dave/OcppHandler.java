package com.dave;

import com.dave.Exception.HttpParseException;
import com.dave.Exception.OcppProtocolException;
import com.dave.Exception.ProtocolException;
import com.dave.Ocpp.OcppProtocol;
import com.dave.Ocpp.OcppProtocol_v16;
import com.dave.StreamProcessor.HTTPReq;
import com.dave.StreamProcessor.HttpStreamProcessor;
import com.dave.StreamProcessor.StreamProcessor;
import com.dave.StreamProcessor.WebSocketStreamProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class OcppHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientInetAddress;
    private boolean protocolUpgraded = false;

    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final HttpStreamProcessor httpStreamProcessor;
    private final WebSocketStreamProcessor webSocketStreamProcessor;

    private final String ocppVersion = "ocpp1.6"; // TODO refactor! set dynamically based on agreed upon version
    private OcppProtocol ocppProtocol;

    public OcppHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientInetAddress = clientSocket.getInetAddress().toString();
        try {
            this.inputStream = clientSocket.getInputStream();
            this.outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.httpStreamProcessor = new HttpStreamProcessor(inputStream, outputStream);
        this.webSocketStreamProcessor = new WebSocketStreamProcessor(inputStream, outputStream);
        System.out.println("Connected to " + clientInetAddress + "\n");
    }

    @Override
    public void run() {
        StreamProcessor streamProcessor;
        while (clientSocket.isConnected()) {
            streamProcessor = this.protocolUpgraded ? this.webSocketStreamProcessor : this.httpStreamProcessor;
            String msg;
            try {
                msg = streamProcessor.read();
            } catch (ProtocolException e) {
                closeClientSocket();
                break;
            }
            logIncoming(msg);
            this.handleReq(msg);
        }
    }

    private void handleReq(String msg) {
        try {
            if (!protocolUpgraded) {
                handleHttpUpgradeReq(msg);
            } else {
                this.ocppProtocol.onMsg(msg);
            }
        } catch (ProtocolException e) {
            System.out.println("Ocpp protocol exception: " + e.getMessage());
            this.closeClientSocket();
        }
    }

    private void closeClientSocket() {
        this.protocolUpgraded = false;
        try {
            System.out.println("Closing connection to " + clientInetAddress + "\n");
            clientSocket.close();
        } catch (IOException ex) {
            this.protocolUpgraded = false;
            throw new RuntimeException(ex);
        }
    }

    private void handleHttpUpgradeReq(String msg) throws ProtocolException {
        HTTPReq req;
        try {
            req = HTTPReq.parse(msg);
        } catch (HttpParseException e) {
            this.protocolUpgraded = false;
            throw new RuntimeException(e);
        }
        if (
                req.headers().getOrDefault("Upgrade", "").equals("websocket")
                        && req.headers().getOrDefault("Connection", "").equals("Upgrade")
                        && req.headers().containsKey("Sec-WebSocket-Key")
                        && req.headers().containsKey("Sec-WebSocket-Protocol")
                        && req.headers().containsKey("Sec-WebSocket-Version")
        ) {
            this.confirmUpgradeReq(req.headers().get("Sec-WebSocket-Key"));

            String requestedProtocols = req.headers().get("Sec-WebSocket-Protocol");
            if (requestedProtocols.isBlank() || !requestedProtocols.contains("ocpp1.6")) { // TODO refactor once other versions implemented
                throw new OcppProtocolException("Requested OCPP versions: '" + requestedProtocols + "' not supported / yet implemented");
            } else {
                this.ocppProtocol = new OcppProtocol_v16(this.webSocketStreamProcessor);
            }
        } else {
            System.out.println("Http upgrade message did not contain the necessary OCPP headers.");
            closeClientSocket();
        }
    }

    private void confirmUpgradeReq(String secWebSocketKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Upgrade", "websocket");
        headers.put("Connection", "Upgrade");
        headers.put("Sec-WebSocket-Accept", calcSecWebSocketAccept(secWebSocketKey));
        headers.put("Sec-WebSocket-Protocol", ocppVersion);

        String upgradeConf = new HTTPReq(
                new String[]{"HTTP/1.1", "101", "Switching Protocols"},
                headers,
                ""
        ).toString();

        logOutgoing(upgradeConf);
        try {
            this.httpStreamProcessor.send(upgradeConf);
        } catch (IOException e) {
            System.out.println("Could not send upgrade request to " + this.clientInetAddress + "\n");
            throw new RuntimeException(e); // TODO what to do here, close connection here or maybe propagate exception up
        }
        this.protocolUpgraded = true;
    }

    public String calcSecWebSocketAccept(String secWebSocketKey) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hashed = sha1.digest((secWebSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            this.protocolUpgraded = false;
            throw new RuntimeException("Failed to compute Sec-WebSocket-Accept", e);
        }
    }

    private void logIncoming(String req) {
        printReq(req, " Receiving from: " + this.clientInetAddress + ":");
    }

    private void logOutgoing(String req) {
        printReq(req, "---- Sending to: " + this.clientInetAddress + ":");
    }

    private void printReq(String req, String info) {
        System.out.println("-----------------------------------" + info);
        System.out.println(req);
        System.out.println("-------------------------------------------------------------------\n");
    }

}
