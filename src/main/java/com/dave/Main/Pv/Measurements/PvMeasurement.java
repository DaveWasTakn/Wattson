package com.dave.Main.Pv.Measurements;

import com.dave.Main.Pv.PvSystem;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public final class PvMeasurement {

    @Id
    private Instant timestamp;
    private int currentWattProduction;
    private int currentWattConsumption;
    private int batteryWattPower;
    private int gridWattPower;
    private int batterySocPercent;
    private long lastMeterUpdateEpoch;

    public PvMeasurement(
            Instant timestamp,
            int currentWattProduction,
            int currentWattConsumption,
            int batteryWattPower,
            int gridWattPower,
            int batterySocPercent,
            long lastMeterUpdateEpoch
    ) {
        this.timestamp = timestamp;
        this.currentWattProduction = currentWattProduction;
        this.currentWattConsumption = currentWattConsumption;
        this.batteryWattPower = batteryWattPower;
        this.gridWattPower = gridWattPower;
        this.batterySocPercent = batterySocPercent;
        this.lastMeterUpdateEpoch = lastMeterUpdateEpoch;
    }

    public PvMeasurement() {

    }

    public static PvMeasurement fromPvSystem(PvSystem pvSystem) {
        return new PvMeasurement(
                Instant.now(),
                pvSystem.getCurrentWattProduction(),
                pvSystem.getCurrentWattConsumption(),
                pvSystem.getBatteryWattPower(),
                pvSystem.getGridWattPower(),
                pvSystem.getBatterySocPercent(),
                pvSystem.getLastMeterUpdateEpoch()
        );
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getCurrentWattProduction() {
        return currentWattProduction;
    }

    public void setCurrentWattProduction(int currentWattProduction) {
        this.currentWattProduction = currentWattProduction;
    }

    public int getCurrentWattConsumption() {
        return currentWattConsumption;
    }

    public void setCurrentWattConsumption(int currentWattConsumption) {
        this.currentWattConsumption = currentWattConsumption;
    }

    public int getBatteryWattPower() {
        return batteryWattPower;
    }

    public void setBatteryWattPower(int batteryWattPower) {
        this.batteryWattPower = batteryWattPower;
    }

    public int getGridWattPower() {
        return gridWattPower;
    }

    public void setGridWattPower(int gridWattPower) {
        this.gridWattPower = gridWattPower;
    }

    public int getBatterySocPercent() {
        return batterySocPercent;
    }

    public void setBatterySocPercent(int batterySocPercent) {
        this.batterySocPercent = batterySocPercent;
    }

    public long getLastMeterUpdateEpoch() {
        return lastMeterUpdateEpoch;
    }

    public void setLastMeterUpdateEpoch(long lastMeterUpdateEpoch) {
        this.lastMeterUpdateEpoch = lastMeterUpdateEpoch;
    }
}
