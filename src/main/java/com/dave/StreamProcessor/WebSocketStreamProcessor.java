package com.dave.StreamProcessor;

import com.dave.Exception.WebSocketProtocolException;
import com.dave.Logging.Logger;
import com.dave.util.Tuple2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WebSocketStreamProcessor implements StreamProcessor {
    private static final Logger LOGGER = Logger.INSTANCE;

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final String clientIp;

    public WebSocketStreamProcessor(InputStream inputStream, OutputStream outputStream, String clientIp) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.clientIp = clientIp;
    }

/* Websocket Base Framing Protocol:

    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-------+-+-------------+-------------------------------+
   |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
   |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
   |N|V|V|V|       |S|             |   (if payload len==126/127)   |
   | |1|2|3|       |K|             |                               |
   +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
   |     Extended payload length continued, if payload len == 127  |
   + - - - - - - - - - - - - - - - +-------------------------------+
   |                               |Masking-key, if MASK set to 1  |
   +-------------------------------+-------------------------------+
   | Masking-key (continued)       |          Payload Data         |
   +-------------------------------- - - - - - - - - - - - - - - - +
   :                     Payload Data continued ...                :
   + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
   |                     Payload Data continued ...                |
   +---------------------------------------------------------------+

source: https://datatracker.ietf.org/doc/html/rfc6455#section-5.2
*/

    @Override
    public String read() throws WebSocketProtocolException {
        final Tuple2<byte[], Integer> res = parseWebsocketFrames();
        final byte[] payload = res.first();
        final int opcode = res.second();

        return switch (opcode) {
            case 0x8 -> {
                handleCloseFrame(payload);
                throw new WebSocketProtocolException("Close frame received.");
            }
            case 0x9 -> {
                handlePingFrame(payload);
                yield read();
            }
            case 0xA -> {
                handlePongFrame(payload);
                yield read();
            }
            default -> new String(payload, StandardCharsets.UTF_8);
        };
    }

    @Override
    public void send(String message) throws IOException {
        LOGGER.logOutgoingMsg(message, this.clientIp);

        // don't fragment websocket frames ... shouldn't be necessary
        // a server MUST NOT mask frames

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // FIN = 1, RSV values = 0, opcode = 1 (text)
        // 1000 0001 = 0x81
        this.outputStream.write(0x81);

        // Mask = 0 + Payload length (depending on message size)
        int msgLen = messageBytes.length;
        if (msgLen <= 125) {
            this.outputStream.write(msgLen); // length is positive and max 7 bits, so MSB (MASK) is always 0
        } else if (msgLen <= 0xFFFF) { // length fits into unsigned 16 bit
            this.outputStream.write(126);
            this.outputStream.write((msgLen >> 8) & 0xFF);
            this.outputStream.write(msgLen & 0xFF);
        } else { // 64-bit
            this.outputStream.write(127);
            ByteBuffer bb = ByteBuffer.allocate(8);
            bb.putLong(messageBytes.length);
            this.outputStream.write(bb.array());
        }

        this.outputStream.write(messageBytes);
        this.outputStream.flush();
    }

    private Tuple2<byte[], Integer> parseWebsocketFrames() throws WebSocketProtocolException {
        // https://datatracker.ietf.org/doc/html/rfc6455#section-5
        try {
            byte byte1 = readByte();
            boolean FIN = bitAt(byte1, 0);
            boolean RSV1 = bitAt(byte1, 1);
            boolean RSV2 = bitAt(byte1, 2);
            boolean RSV3 = bitAt(byte1, 3);
            int OPCODE = byte1 & ((1 << 4) - 1);

            byte byte2 = readByte();
            boolean MASK = bitAt(byte2, 0);

            if (!MASK) {
                throw new WebSocketProtocolException("Client requests need to be masked.");
            }

            long PAYLOAD_LEN = byte2 & 0x7F;
            if (PAYLOAD_LEN == 126) {
                byte[] extendedLength = readBytes(2);
                PAYLOAD_LEN = ((extendedLength[0] & 0xFF) << 8) | (extendedLength[1] & 0xFF);
            } else if (PAYLOAD_LEN == 127) {
                byte[] extendedLength = readBytes(8);
                PAYLOAD_LEN = ByteBuffer.wrap(extendedLength).getLong(); // ByteOrder.BIG_ENDIAN by default ?!
            }

            byte[] MASKING_KEY = readBytes(4);

            // no extension data -> only application data
            if (PAYLOAD_LEN > Integer.MAX_VALUE) {
                // very unlikely > 2GB payload ==> just cast to int for now
                // theoretically need to read in chunks
                throw new WebSocketProtocolException("Client request too large");
            }
            byte[] payload = readBytes((int) PAYLOAD_LEN);
            unmaskPayload(payload, MASKING_KEY);

            if (FIN) {
                return new Tuple2<>(payload, OPCODE);
            } else { // instead of recursion could use while(!FIN) and ByteArrayOutputStream for better efficiency
                byte[] nextPayload = parseWebsocketFrames().first();
                return new Tuple2<>(
                        ByteBuffer.allocate(payload.length + nextPayload.length).put(payload).put(nextPayload).array(),
                        OPCODE
                );
            }
        } catch (IOException e) {
            throw new WebSocketProtocolException(e.getMessage());
        }
    }

    private byte readByte() throws IOException, WebSocketProtocolException {
        return readBytes(1)[0];
    }

    private byte[] readBytes(int num) throws IOException, WebSocketProtocolException {
        byte[] bytes = this.inputStream.readNBytes(num);
        if (bytes.length != num) {
            throw new WebSocketProtocolException("Could not read websocket frame: connection was closed.");
        }
        return bytes;
    }

    private static void unmaskPayload(byte[] payloadBytes, byte[] maskingKey) { // unmask payloadBytes inPlace
        byte[] unmasked = new byte[payloadBytes.length];
        for (int i = 0; i < payloadBytes.length; i++) {
            payloadBytes[i] = (byte) (payloadBytes[i] ^ maskingKey[i % 4]);
        }
    }

    private static boolean bitAt(byte b, int pos) { // MSB to LSB;
        if (pos < 0 || pos > 7) {
            throw new IllegalArgumentException("pos must be 0..7");
        }
        return (b & (1 << (7 - pos))) != 0;
    }

    private void handleCloseFrame(byte[] payload) {
        try {
            LOGGER.logIncomingMsg(getCloseFrameLogRepresentation(payload), this.clientIp);
            sendCloseFrame(payload); // echo the close frame back
        } catch (IOException e) {
            // its ok if it could not be sent; client may not accept any more messages
        }
    }

    private static String getCloseFrameLogRepresentation(byte[] payload) {
        int statusCode = 1005; // https://datatracker.ietf.org/doc/html/rfc6455#section-7.4
        String msg = "";
        if (payload.length >= 2) {
            statusCode = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF);
            msg = new String(payload, 2, payload.length - 2, StandardCharsets.UTF_8);
        }
        return "CloseFrame(StatusCode=" + statusCode + ", Message=" + msg + ")";
    }

    private void handlePingFrame(byte[] payload) throws WebSocketProtocolException {
        try {
            LOGGER.logIncomingMsg("PingFrame", clientIp);
            sendPongFrame(payload);
        } catch (IOException e) {
            throw new WebSocketProtocolException("Failed sending pong frame");
        }
    }

    private void handlePongFrame(byte[] payload) {
        LOGGER.logIncomingMsg("PongFrame", clientIp);
    }

    private void sendCloseFrame(byte[] payload) throws IOException {
        LOGGER.logOutgoingMsg(getCloseFrameLogRepresentation(payload), this.clientIp);
        sendControlFrame((byte) 0x8, payload);
    }

    public void sendCloseFrame(int statusCode, String msg) throws IOException {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[2 + msgBytes.length];
        payload[0] = (byte) ((statusCode >> 8) & 0xFF);
        payload[1] = (byte) (statusCode & 0xFF);
        System.arraycopy(msgBytes, 0, payload, 2, msgBytes.length);
        LOGGER.logOutgoingMsg(getCloseFrameLogRepresentation(payload), this.clientIp);
        sendControlFrame((byte) 0x8, payload);
    }

    public void sendPingFrame(byte[] payload) throws IOException {
        sendControlFrame((byte) 0x9, payload);
    }

    private void sendPongFrame(byte[] payload) throws IOException {
        sendControlFrame((byte) 0xA, payload);
    }

    private void sendControlFrame(byte opcode, byte[] payload) throws IOException {
        // FIN=1, RSVs = 0
        outputStream.write(0x80 | opcode);
        outputStream.write(payload.length);
        outputStream.write(payload);
        outputStream.flush();
    }

}
