package com.dave.Main.Pv.Measurements;

import com.dave.Main.Pv.PvSystem;
import com.dave.Main.State.Observe.Event.PvSystemStatusUpdateEvent;
import com.dave.Main.State.Observe.Observer;
import com.dave.Main.State.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PvMeasurementPersister implements Observer<PvSystemStatusUpdateEvent> {

    private final State state;
    private final PvSystem pvSystem;
    private final PvMeasurementsRepository pvMeasurementsRepository;

    @Autowired
    public PvMeasurementPersister(State state, PvSystem pvSystem, PvMeasurementsRepository pvMeasurementsRepository) {
        this.state = state;
        this.pvSystem = pvSystem;
        this.pvMeasurementsRepository = pvMeasurementsRepository;
        this.state.subscribe(this, PvSystemStatusUpdateEvent.class);
    }

    @Override
    public void onNotify(PvSystemStatusUpdateEvent event) {
        PvMeasurement pvMeasurement = PvMeasurement.fromPvSystem(this.pvSystem);
        this.pvMeasurementsRepository.save(pvMeasurement);
    }

}
