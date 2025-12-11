package com.dave;

import com.dave.Exception.HttpParseException;
import com.dave.Exception.HttpProtocolException;
import com.dave.Exception.OcppProtocolException;
import com.dave.Exception.ProtocolException;
import com.dave.Logging.Logger;
import com.dave.Ocpp.OccpSpec;
import com.dave.Ocpp.OccpSpec_v16;
import com.dave.State.ChargePoint;
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

public class OcppSocketHandler implements Runnable {
    private static final Logger LOGGER = Logger.INSTANCE;

    private final Socket socket;
    private final String clientIp;
    private final ChargePoint chargePoint;
    private boolean protocolUpgraded = false;

    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final HttpStreamProcessor httpStreamProcessor;
    private final WebSocketStreamProcessor webSocketStreamProcessor;

    private OccpSpec occpSpec;

    public OcppSocketHandler(Socket socket) {
        this.socket = socket;
        this.clientIp = socket.getInetAddress().getHostAddress();
        this.chargePoint = new ChargePoint(clientIp);
        try {
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.httpStreamProcessor = new HttpStreamProcessor(inputStream, outputStream);
        this.webSocketStreamProcessor = new WebSocketStreamProcessor(inputStream, outputStream, this.clientIp);
        LOGGER.print("Connected to " + clientIp);
    }

    @Override
    public void run() {
        StreamProcessor streamProcessor;
        while (!socket.isClosed()) {
            streamProcessor = this.protocolUpgraded ? this.webSocketStreamProcessor : this.httpStreamProcessor;
            String msg;
            try {
                msg = streamProcessor.read();
            } catch (ProtocolException e) {
                this.closeClientSocket();
                break;
            }
            LOGGER.logIncomingMsg(msg, this.clientIp);
            this.handleReq(msg);
        }
    }

    private void handleReq(String msg) {
        try {
            if (!protocolUpgraded) {
                handleHttpUpgradeReq(msg);
            } else {
                this.occpSpec.onMsg(msg);
            }
        } catch (ProtocolException e) {
            LOGGER.print("Ocpp protocol exception: " + e.getMessage());
            this.closeClientSocket();
        }
    }

    private void closeClientSocket() {
        try {
            LOGGER.print("Closing connection to " + clientIp);
            // TODO send closing message?
            socket.close();
        } catch (IOException ex) {
            this.protocolUpgraded = false;
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

            String requestedProtocols = req.headers().get("Sec-WebSocket-Protocol");
            if (requestedProtocols.isBlank() || !requestedProtocols.contains("ocpp1.6")) { // TODO refactor once other versions implemented
                throw new OcppProtocolException("Requested OCPP versions: '" + requestedProtocols + "' not supported / yet implemented");
            } else {
                this.confirmUpgradeReq("ocpp1.6", req.headers().get("Sec-WebSocket-Key"));
                this.occpSpec = new OccpSpec_v16(this.webSocketStreamProcessor, this.chargePoint);
            }
        } else {
            LOGGER.print("Http upgrade message did not contain the necessary OCPP headers.");
            this.closeClientSocket();
        }
    }

    private void confirmUpgradeReq(String ocppVersion, String secWebSocketKey) throws HttpProtocolException {
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

        LOGGER.logOutgoingMsg(upgradeConf, this.clientIp);
        try {
            this.httpStreamProcessor.send(upgradeConf);
        } catch (IOException e) {
            LOGGER.print("Could not send upgrade request to " + this.clientIp);
            throw new HttpProtocolException("Could not send upgrade request to " + this.clientIp);
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

}
