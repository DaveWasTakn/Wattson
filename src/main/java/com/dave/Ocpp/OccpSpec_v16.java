package com.dave.Ocpp;

import com.dave.Entity.ChargePoint;
import com.dave.Entity.Connector;
import com.dave.Entity.Status;
import com.dave.Ocpp.Message.CallMsg;
import com.dave.Ocpp.Message.CallResultMsg;
import com.dave.StreamProcessor.StreamProcessor;
import com.dave.Util.Utils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OccpSpec_v16 extends OccpSpec {

    public OccpSpec_v16(StreamProcessor streamProcessor, ChargePoint chargePoint) {
        super(streamProcessor, chargePoint);
    }

    /// ////////////////////////////////////////////////////////////////////////

    public void onCall_Authorize(CallMsg message) throws IOException {
        String idTag = message.payload().get("idTag").stringValue();
        LOGGER.print("Authorize request received for id: '" + idTag + "'");

        // TODO check authorize; database? config-file?

        ObjectNode idTagInfo = new ObjectMapper().createObjectNode();
        idTagInfo.put("status", AuthorizationStatus.ACCEPTED.getDisplayName());
        idTagInfo.put("expiryDate", Utils.dateTimePlusMinutes(5)); // TODO change
        // idTagInfo.put("parentIdTag", ??);
        ObjectNode payload = createPayload("idTagInfo", idTagInfo);
        this.streamProcessor.send(new CallResultMsg(message.uniqueId(), payload).serialize());
    }

    public void onCall_BootNotification(CallMsg message) throws IOException {
        fillAvailableFields(message.payload(), this.chargePoint);

        ObjectNode payload = createPayload(
                "currentTime", Utils.dateTime(),
                "interval", 60, // TODO need interval for heartbeat check
                "status", RegistrationStatus.ACCEPTED.getDisplayName()
        );

        this.streamProcessor.send(new CallResultMsg(message.uniqueId(), payload).serialize());
    }

    public void onCall_DataTransfer(CallMsg message) {
        LOGGER.print("DataTransfer received");
    }

    public void onCall_DiagnosticsStatusNotification(CallMsg message) {
        LOGGER.print("DiagnosticsStatusNotification received");
    }

    public void onCall_FirmwareStatusNotification(CallMsg message) {
        LOGGER.print("FirmwareStatusNotification received");
    }

    public void onCall_Heartbeat(CallMsg message) throws IOException {
        // TODO track if chargepoint is alive; register timer somewhere ? but need interval
        this.chargePoint.setLastHeartbeat(Instant.now());
        this.streamProcessor.send(currentTimeCallResult(message.uniqueId()));
    }

    public void onCall_MeterValues(CallMsg message) {
        LOGGER.print("MeterValues received");
    }

    public void onCall_StartTransaction(CallMsg message) {
        LOGGER.print("StartTransaction received");
    }

    public void onCall_StatusNotification(CallMsg message) throws IOException {
        int connectorId = message.payload().get("connectorId").intValue();
        Status status = new Status();
        fillAvailableFields(message.payload(), status);

        if (connectorId == 0) { // connectorId == 0 is reserved for the whole charge point
            this.chargePoint.setStatus(status);
        } else { // actual connector
            this.chargePoint.updateConnector(new Connector(connectorId, status));
        }

        this.streamProcessor.send(new CallResultMsg(message.uniqueId(), new ObjectMapper().createObjectNode()).serialize());
    }

    public void onCall_StopTransaction(CallMsg message) {
        LOGGER.print("StopTransaction received");
    }

    /// ////////////////////////////////////////////////////////////////////////

    // TODO handle the requests in separate (virtual)threads and somehow register a callback or a something to relate onCallResult and onCallError to that request using the unique message id
    public void req_CancelReservation() {
        LOGGER.print("CancelReservation requested");
    }

    public void req_ChangeAvailability() {
        LOGGER.print("ChangeAvailability requested");
    }

    public void req_ChangeConfiguration() {
        LOGGER.print("ChangeConfiguration requested");
    }

    public void req_ClearCache() {
        LOGGER.print("ClearCache requested");
    }

    public void req_ClearChargingProfile() {
        LOGGER.print("ClearChargingProfile requested");
    }

    public void req_DataTransfer() {
        LOGGER.print("DataTransfer requested");
    }

    public void req_GetCompositeSchedule() {
        LOGGER.print("GetCompositeSchedule requested");
    }

    public void req_GetConfiguration() {
        LOGGER.print("GetConfiguration requested");
    }

    public void req_GetDiagnostics() {
        LOGGER.print("GetDiagnostics requested");
    }

    public void req_GetLocalListVersion() {
        LOGGER.print("GetLocalListVersion requested");
    }

    public void req_RemoteStartTransaction() {
        LOGGER.print("RemoteStartTransaction requested");
    }

    public void req_RemoteStopTransaction() {
        LOGGER.print("RemoteStopTransaction requested");
    }

    public void req_ReserveNow() {
        LOGGER.print("ReserveNow requested");
    }

    public void req_Reset() {
        LOGGER.print("Reset requested");
    }

    public void req_SendLocalList() {
        LOGGER.print("SendLocalList requested");
    }

    public void req_SetChargingProfile() {
        LOGGER.print("SetChargingProfile requested");
    }

    public void req_TriggerMessage() {
        LOGGER.print("TriggerMessage requested");
    }

    public void req_UnlockConnector() {
        LOGGER.print("UnlockConnector requested");
    }

    public void req_UpdateFirmware() {
        LOGGER.print("UpdateFirmware requested");
    }

    /// ////////////////////////////////////////////////////////////////////////

    private static ObjectNode createPayload(Object... kvs) {
        if (kvs.length % 2 != 0) {
            throw new IllegalArgumentException("Key value pairs must be an even number");
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        for (int i = 0; i < kvs.length; i += 2) {
            payload.set((String) kvs[i], mapper.valueToTree(kvs[i + 1]));
        }
        return payload;
    }

    private static String currentTimeCallResult(String uniqueId) {
        return new CallResultMsg(uniqueId, createPayload("currentTime", Utils.dateTime())).serialize();
    }

    private static void fillAvailableFields(ObjectNode src, Object dest) {
        Map<String, Field> fields = Arrays.stream(dest.getClass().getDeclaredFields()).collect(Collectors.toMap(Field::getName, Function.identity()));
        src.propertyStream().forEach(entry -> {
            if (fields.containsKey(entry.getKey())) {
                Field f = fields.get(entry.getKey());
                f.setAccessible(true);
                try {
                    Class<?> fType = f.getType();
                    JsonNode value = entry.getValue();
                    if (fType.isEnum()) {
                        try {
                            Method fromValue = fType.getDeclaredMethod("fromValue", String.class);
                            fromValue.setAccessible(true);
                            Object enumValue = fromValue.invoke(null, value.stringValue());
                            f.set(dest, enumValue);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException("No fromValue method found for Enum: " + fType.getName());
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException("Could not invoke fromValue method for Enum: " + fType.getName() + ":\n" + e.getMessage());
                        }
                    } else if (fType == String.class) {
                        f.set(dest, value.stringValue());
                    } else if (fType == boolean.class || fType == Boolean.class) {
                        f.set(dest, value.asBoolean());
                    } else if (fType == int.class || fType == Integer.class) {
                        f.set(dest, value.asInt());
                    } else if (fType == long.class || fType == Long.class) {
                        f.set(dest, value.asLong());
                    } else if (fType == double.class || fType == Double.class) {
                        f.set(dest, value.asDouble());
                    } else if (fType == Instant.class) {
                        f.set(dest, Instant.parse(value.stringValue()));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
