package com.dave.Main.State;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class State implements Observer, Observable {
    private final List<ChargePoint> chargePoints = new ArrayList<>();

    private final List<Observer> observers = new ArrayList<>();

    public void onNotify() {
        this.notifyObservers();
    }

    public void registerChargePoint(ChargePoint chargePoint) {
        chargePoint.addObserver(this);
        this.chargePoints.add(chargePoint);
        this.notifyObservers();
    }

    public List<ChargePoint> getChargePoints() {
        return chargePoints;
    }

    public void removeChargePoint(ChargePoint chargePoint) {
        this.chargePoints.remove(chargePoint);
        this.notifyObservers();
    }

    @Override
    public String toString() {
        return "State{" +
                "chargePoints=" + chargePoints +
                '}';
    }

    @Override
    public void addObserver(Observer o) {
        this.observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        this.observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        this.observers.forEach(Observer::onNotify);
    }
}
