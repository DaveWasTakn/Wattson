package com.dave.Ocpp;

import com.dave.StreamProcessor.StreamProcessor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.time.Instant;

public class OcppProtocol_v16 extends OcppProtocol {

    public OcppProtocol_v16(StreamProcessor streamProcessor) {
        super(streamProcessor);
    }

    /// ////////////////////////////////////////////////////////////////////////

    public void onCall_Authorize(CallMsg message) {
        System.out.println("Authorize received");
    }

    public void onCall_BootNotification(CallMsg message) throws IOException {
        // TODO read info into a ChargePoint-obj
        ObjectNode payload = new ObjectMapper().createObjectNode();
        payload.put("currentTime", Instant.now().toString());
        payload.put("interval", 60);
        payload.put("status", RegistrationStatus.ACCEPTED.getDisplayName());

        this.streamProcessor.send(new CallResultMsg(3, message.uniqueId(), payload).serialize());
        System.out.println("BootNotification received");
    }

    public void onCall_DataTransfer(CallMsg message) {
        System.out.println("DataTransfer received");
    }

    public void onCall_DiagnosticsStatusNotification(CallMsg message) {
        System.out.println("DiagnosticsStatusNotification received");
    }

    public void onCall_FirmwareStatusNotification(CallMsg message) {
        System.out.println("FirmwareStatusNotification received");
    }

    public void onCall_Heartbeat(CallMsg message) {
        System.out.println("Heartbeat received");
    }

    public void onCall_MeterValues(CallMsg message) {
        System.out.println("MeterValues received");
    }

    public void onCall_StartTransaction(CallMsg message) {
        System.out.println("StartTransaction received");
    }

    public void onCall_StatusNotification(CallMsg message) {
        System.out.println("StatusNotification received");
    }

    public void onCall_StopTransaction(CallMsg message) {
        System.out.println("StopTransaction received");
    }

    /// ////////////////////////////////////////////////////////////////////////

    // TODO handle the requests in separate (virtual)threads and somehow register a callback or a something to relate onCallResult and onCallError to that request using the unique message id
    public void req_CancelReservation() {
        System.out.println("CancelReservation requested");
    }

    public void req_ChangeAvailability() {
        System.out.println("ChangeAvailability requested");
    }

    public void req_ChangeConfiguration() {
        System.out.println("ChangeConfiguration requested");
    }

    public void req_ClearCache() {
        System.out.println("ClearCache requested");
    }

    public void req_ClearChargingProfile() {
        System.out.println("ClearChargingProfile requested");
    }

    public void req_DataTransfer() {
        System.out.println("DataTransfer requested");
    }

    public void req_GetCompositeSchedule() {
        System.out.println("GetCompositeSchedule requested");
    }

    public void req_GetConfiguration() {
        System.out.println("GetConfiguration requested");
    }

    public void req_GetDiagnostics() {
        System.out.println("GetDiagnostics requested");
    }

    public void req_GetLocalListVersion() {
        System.out.println("GetLocalListVersion requested");
    }

    public void req_RemoteStartTransaction() {
        System.out.println("RemoteStartTransaction requested");
    }

    public void req_RemoteStopTransaction() {
        System.out.println("RemoteStopTransaction requested");
    }

    public void req_ReserveNow() {
        System.out.println("ReserveNow requested");
    }

    public void req_Reset() {
        System.out.println("Reset requested");
    }

    public void req_SendLocalList() {
        System.out.println("SendLocalList requested");
    }

    public void req_SetChargingProfile() {
        System.out.println("SetChargingProfile requested");
    }

    public void req_TriggerMessage() {
        System.out.println("TriggerMessage requested");
    }

    public void req_UnlockConnector() {
        System.out.println("UnlockConnector requested");
    }

    public void req_UpdateFirmware() {
        System.out.println("UpdateFirmware requested");
    }

}
