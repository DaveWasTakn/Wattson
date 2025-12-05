package com.dave.StreamProcessor;

import com.dave.Exception.HttpProtocolException;
import com.dave.Exception.ProtocolException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpStreamProcessor implements StreamProcessor {

    @Override
    public String read(InputStream inputStream) throws ProtocolException {
        StringBuilder sb = new StringBuilder();
        // read until we reach the end of a http message: "\r\n\r\n"
        try {
            int c;
            while ((c = inputStream.read()) != -1) {
                sb.append((char) c);
                if (
                        sb.length() >= 4
                                && sb.charAt(sb.length() - 1) == '\n'
                                && sb.charAt(sb.length() - 2) == '\r'
                                && sb.charAt(sb.length() - 3) == '\n'
                                && sb.charAt(sb.length() - 4) == '\r'
                ) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new HttpProtocolException(e.getMessage());
        }
        return sb.toString();
    }

    @Override
    public void send(String message, OutputStream outputStream) throws IOException {
//        try {
            outputStream.write(message.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
//        } catch (IOException e) {
//            System.out.println("Could not send message");
//            throw new RuntimeException(e);
//        }
    }

}
