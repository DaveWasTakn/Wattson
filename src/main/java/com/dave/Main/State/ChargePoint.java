package com.dave.Main.State;

import com.dave.Main.State.Observe.ChargePointEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ChargePoint {

    private final State state;

    private String ipAddress;

    @Deprecated
    private String chargeBoxSerialNumber;
    private String chargePointModel;
    private String chargePointSerialNumber;
    private String chargePointVendor;
    private String firmwareVersion;
    private String iccid;
    private String imsi;
    private String iccd;
    private String meterSerialNumber;
    private String meterType;

    private Instant lastHeartbeat; // TODO create isAlive() method and check
    private Status status;
    private final Map<Integer, Connector> connectors = new HashMap<>();

    public ChargePoint(State state, String ipAddress) {
        this.state = state;
        this.ipAddress = ipAddress;
        this.state.registerChargePoint(this);
    }

    public String getChargeBoxSerialNumber() {
        return chargeBoxSerialNumber;
    }

    public void setChargeBoxSerialNumber(String chargeBoxSerialNumber) {
        this.chargeBoxSerialNumber = chargeBoxSerialNumber;
        this.dispatchChargepointEvent();
    }

    public String getModel() {
        return chargePointModel;
    }

    public void setModel(String model) {
        this.chargePointModel = model;
        this.dispatchChargepointEvent();
    }

    public String getChargePointSerialNumber() {
        return chargePointSerialNumber;
    }

    public void setChargePointSerialNumber(String chargePointSerialNumber) {
        this.chargePointSerialNumber = chargePointSerialNumber;
        this.dispatchChargepointEvent();
    }

    public String getVendor() {
        return chargePointVendor;
    }

    public void setVendor(String vendor) {
        this.chargePointVendor = vendor;
        this.dispatchChargepointEvent();
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
        this.dispatchChargepointEvent();
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
        this.dispatchChargepointEvent();
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
        this.dispatchChargepointEvent();
    }

    public String getIccd() {
        return iccd;
    }

    public void setIccd(String iccd) {
        this.iccd = iccd;
        this.dispatchChargepointEvent();
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public void setMeterSerialNumber(String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
        this.dispatchChargepointEvent();
    }

    public String getMeterType() {
        return meterType;
    }

    public void setMeterType(String meterType) {
        this.meterType = meterType;
        this.dispatchChargepointEvent();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        this.dispatchChargepointEvent();
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
        this.dispatchChargepointEvent();
    }

    public Map<Integer, Connector> getConnectors() {
        return connectors;
    }

    public void updateConnector(Connector connector) {
        this.connectors.put(connector.getConnectorId(), connector);
        this.dispatchChargepointEvent();
    }

    @Override
    public String toString() {
        return "ChargePoint{" +
                "ipAddress='" + ipAddress + '\'' +
                ", chargeBoxSerialNumber='" + chargeBoxSerialNumber + '\'' +
                ", chargePointModel='" + chargePointModel + '\'' +
                ", chargePointSerialNumber='" + chargePointSerialNumber + '\'' +
                ", chargePointVendor='" + chargePointVendor + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", iccid='" + iccid + '\'' +
                ", imsi='" + imsi + '\'' +
                ", iccd='" + iccd + '\'' +
                ", meterSerialNumber='" + meterSerialNumber + '\'' +
                ", meterType='" + meterType + '\'' +
                ", lastHeartbeat=" + lastHeartbeat +
                ", status=" + status +
                ", connectors=" + connectors +
                '}';
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.dispatchChargepointEvent();
    }

    public void dispatchChargepointEvent() {
        this.state.publish(new ChargePointEvent());
    }
}
