package com.dave.Ocpp;

import com.dave.Exception.OcppProtocolException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

public sealed interface OcppMessage permits CallMsg, CallResultMsg, CallErrorMsg {

    ObjectMapper objectMapper = new ObjectMapper();

    static OcppMessage fromMessage(String msg) throws OcppProtocolException {
        throw new UnsupportedOperationException("Only callable on actual implementations");
    }

    static List<ObjectNode> getMsgItems(String msg) throws OcppProtocolException {
        try {
            return objectMapper.readValue(msg, new TypeReference<List<ObjectNode>>() {
            }); // keep explicit type List<ObjectNode> !!!
        } catch (JacksonException e) {
            throw new OcppProtocolException("Could not parse OCPP message: " + e.getMessage());
        }
    }

    String serialize();

}
