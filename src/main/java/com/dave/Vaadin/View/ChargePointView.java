package com.dave.Vaadin.View;

import com.dave.Main.State.ChargePoint;
import com.dave.Main.State.Observer;
import com.dave.Main.State.State;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/chargePoints")
@PageTitle("Charge Points")
@Menu(order = 0, icon = "vaadin:connect", title = "ChargePoints")
public class ChargePointView extends VerticalLayout implements Observer {

    private final State state;

    private UI ui;
    private final Grid<ChargePoint> chargePointGrid;

    @Autowired
    public ChargePointView(State state) {
        this.state = state;
        this.state.addObserver(this);

        this.chargePointGrid = new Grid<>(ChargePoint.class, true);
        add(this.chargePointGrid);

        updateView();
    }

    private void updateView() {
        chargePointGrid.setItems(this.state.getChargePoints());
    }

    @Override
    public void onNotify() {
        if (this.ui != null) {
            this.ui.access(this::updateView);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        this.ui = attachEvent.getUI();
        super.onAttach(attachEvent);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        this.ui = null;
        super.onDetach(detachEvent);
    }
}
