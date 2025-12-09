package com.dave.Ocpp;

import com.dave.Exception.OcppProtocolException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

public record CallErrorMsg(
        int messageTypeId,
        String uniqueId,
        String errorCode,
        String errorDescription,
        JsonNode errorDetails
) implements OcppMessage {
    // CallError: [<MessageTypeId>, "<UniqueId>", "<errorCode>", "<errorDescription>", {<errorDetails>}]

    public static CallErrorMsg fromMessage(String msg) throws OcppProtocolException {
        List<JsonNode> items = OcppMessage.getMsgItems(msg);
        if (items.size() != 5) {
            throw new OcppProtocolException("CallErrorMsg is malformed");
        }
        return new CallErrorMsg(
                items.get(0).intValue(),
                items.get(1).stringValue(),
                items.get(2).stringValue(),
                items.get(3).stringValue(),
                items.get(4)
        );
    }

    @Override
    public String serialize() {
        return new ObjectMapper().valueToTree(List.of(
                this.messageTypeId,
                this.uniqueId,
                this.errorCode,
                this.errorDescription,
                this.errorDetails
        )).toString();
    }
}
