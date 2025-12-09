package com.dave.Ocpp;

import com.dave.Exception.OcppProtocolException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

public record CallMsg(
        int messageTypeId,
        String uniqueId,
        String action,
        JsonNode payload
) implements OcppMessage {
    // Call: [<MessageTypeId>, "<UniqueId>", "<Action>", {<Payload>}]

    public static CallMsg fromMessage(String msg) throws OcppProtocolException {
        List<JsonNode> items = OcppMessage.getMsgItems(msg);
        if (items.size() != 4) {
            throw new OcppProtocolException("CallMsg is malformed");
        }
        return new CallMsg(
                items.get(0).intValue(),
                items.get(1).stringValue(),
                items.get(2).stringValue(),
                items.get(3)
        );
    }

    @Override
    public String serialize() {
        return new ObjectMapper().valueToTree(List.of(
                this.messageTypeId,
                this.uniqueId,
                this.action,
                this.payload
        )).toString();
    }
}
