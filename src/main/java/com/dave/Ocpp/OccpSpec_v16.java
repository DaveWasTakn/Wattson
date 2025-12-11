package com.dave.Ocpp;

import com.dave.State.ChargePoint;
import com.dave.StreamProcessor.StreamProcessor;
import com.dave.util.Utils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.Field;
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

    public void onCall_Authorize(CallMsg message) {
        LOGGER.print("Authorize received");
    }

    public void onCall_BootNotification(CallMsg message) throws IOException {
        Map<String, Field> cpFields = Arrays.stream(this.chargePoint.getClass().getDeclaredFields()).collect(Collectors.toMap(Field::getName, Function.identity()));
        message.payload().propertyStream().forEach(e -> {
            if (cpFields.containsKey(e.getKey())) {
                Field f = cpFields.get(e.getKey());
                f.setAccessible(true);
                try {
                    f.set(this.chargePoint, e.getValue().stringValue());
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        ObjectNode payload = createPayload(
                "currentTime", Utils.dateTime(),
                "interval", 60, // TODO need interval for heartbeat check
                "status", RegistrationStatus.ACCEPTED.getDisplayName()
        );

        String callResult = new CallResultMsg(3, message.uniqueId(), payload).serialize();
        this.streamProcessor.send(callResult);
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
        ObjectNode payload = createPayload("currentTime", Utils.dateTime());

        String callResult = new CallResultMsg(3, message.uniqueId(), payload).serialize();
        this.streamProcessor.send(callResult);
    }

    public void onCall_MeterValues(CallMsg message) {
        LOGGER.print("MeterValues received");
    }

    public void onCall_StartTransaction(CallMsg message) {
        LOGGER.print("StartTransaction received");
    }

    public void onCall_StatusNotification(CallMsg message) {
        LOGGER.print("StatusNotification received");
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
        ObjectNode payload = new ObjectMapper().createObjectNode();
        for (int i = 0; i < kvs.length; i += 2) {
            payload.set((String) kvs[i], mapper.valueToTree(kvs[i + 1]));
        }
        return payload;
    }

}
