package com.dave.StreamProcessor;

import com.dave.Exception.WebSocketProtocolException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WebSocketStreamProcessor implements StreamProcessor {

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public WebSocketStreamProcessor(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
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
        byte[] payload = parseWebsocketFrames();
        return new String(payload, StandardCharsets.UTF_8);
    }

    @Override
    public void send(String message) throws IOException {
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

    private byte[] parseWebsocketFrames() throws WebSocketProtocolException {
        // https://datatracker.ietf.org/doc/html/rfc6455#section-5
        try {
            byte byte1 = readByte();
            boolean FIN = bitAt(byte1, 0);
            boolean RSV1 = bitAt(byte1, 1);
            boolean RSV2 = bitAt(byte1, 2);
            boolean RSV3 = bitAt(byte1, 3);
            int OPCODE = byte1 & ((1 << 5) - 1);

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
                // TODO theoretically need to read in chunks
            }
            byte[] payload = readBytes((int) PAYLOAD_LEN);
            unmaskPayload(payload, MASKING_KEY);

            if (FIN) {
                return payload;
            } else { // instead of recursion could use while(!FIN) and ByteArrayOutputStream for better efficiency
                byte[] nextPayload = parseWebsocketFrames();
                return ByteBuffer.allocate(payload.length + nextPayload.length).put(payload).put(nextPayload).array();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO handle opcodes i.e., ping pong
        // TODO opcodes only need ping pong and close since OCPP uses text only; throw ocppprotocolexception on other opcodes
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

}
