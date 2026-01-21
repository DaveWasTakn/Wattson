package com.dave.Main.State;

import com.dave.Main.State.Observe.ChargePointEvent;
import com.dave.Main.State.Observe.Observer;
import com.dave.Main.State.Observe.StateEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class State {
    private final List<ChargePoint> chargePoints = new ArrayList<>();

    private final Map<Class<? extends StateEvent>, List<Observer<? extends StateEvent>>> observers = new ConcurrentHashMap<>();

    public <T extends StateEvent> void subscribe(Observer<T> observer, Class<T> type) {
        this.observers.computeIfAbsent(type, _ -> new ArrayList<>()).add(observer);
    }

    public <T extends StateEvent> void publish(T event) {
        List<Observer<? extends StateEvent>> observers = this.observers.get(event.getClass());
        if (observers != null) {
            observers.forEach(x -> ((Observer<T>) x).onNotify(event));
        }
    }

    public void registerChargePoint(ChargePoint chargePoint) {
        this.chargePoints.add(chargePoint);
        this.publish(new ChargePointEvent()); // TODO dont like this here
    }

    public List<ChargePoint> getChargePoints() {
        return chargePoints;
    }

    public void removeChargePoint(ChargePoint chargePoint) {
        this.chargePoints.remove(chargePoint);
        this.publish(new ChargePointEvent());  // TODO dont like this here
    }

    @Override
    public String toString() {
        return "State{" +
                "chargePoints=" + chargePoints +
                '}';
    }


}
