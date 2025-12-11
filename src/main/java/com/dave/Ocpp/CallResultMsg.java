package com.dave.Ocpp;

import com.dave.Exception.OcppProtocolException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

public record CallResultMsg(
        int messageTypeId,
        String uniqueId,
        ObjectNode payload
) implements OcppMessage {
    // CallResult: [<MessageTypeId>, "<UniqueId>", {<Payload>}]

    public static CallResultMsg fromMessage(String msg) throws OcppProtocolException {
        List<ObjectNode> items = OcppMessage.getMsgItems(msg);
        if (items.size() != 3) {
            throw new OcppProtocolException("CallResultMsg is malformed");
        }
        return new CallResultMsg(
                items.get(0).intValue(),
                items.get(1).stringValue(),
                items.get(2)
        );
    }

    @Override
    public String serialize() {
        return new ObjectMapper().valueToTree(List.of(
                this.messageTypeId,
                this.uniqueId,
                this.payload
        )).toString();
    }
}
