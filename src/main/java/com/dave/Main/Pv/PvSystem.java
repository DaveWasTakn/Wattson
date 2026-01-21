package com.dave.Main.Pv;

public interface PvSystem {

    int getCurrentWattProduction();

    int getCurrentWattConsumption();

    int getBatteryWattPower();

    int getGridWattPower();

    int getBatterySocPercent();

    long getLastMeterUpdateEpoch();

}
