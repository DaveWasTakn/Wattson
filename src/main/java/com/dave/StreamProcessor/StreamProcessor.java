package com.dave.StreamProcessor;

import com.dave.Exception.ProtocolException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamProcessor {

    /**
     * Get the next complete message from the InputStream as String.
     */
    String read(InputStream inputStream) throws ProtocolException;

    /**
     * Send a message to the output stream in the appropriate format.
     */
    void send(String message, OutputStream outputStream) throws IOException;

}
