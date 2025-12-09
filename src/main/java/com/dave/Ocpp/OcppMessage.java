package com.dave.Ocpp;

import com.dave.Exception.OcppProtocolException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

public sealed interface OcppMessage permits CallMsg, CallResultMsg, CallErrorMsg {

    ObjectMapper objectMapper = new ObjectMapper();

    static OcppMessage fromMessage(String msg) throws OcppProtocolException {
        throw new UnsupportedOperationException("Only callable on actual implementations");
    }

    static List<JsonNode> getMsgItems(String msg) throws OcppProtocolException {
        try {
            return objectMapper.readValue(msg, new TypeReference<List<JsonNode>>() {}); // keep explicit type List<JsonNode> !!!
        } catch (JacksonException e) {
            throw new OcppProtocolException("Could not parse OCPP message: " + e.getMessage());
        }
    }

    String serialize();

}
