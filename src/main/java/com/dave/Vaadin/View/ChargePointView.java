package com.dave.Vaadin.View;

import com.dave.Main.State.ChargePoint;
import com.dave.Main.State.Observe.Event.ChargePointEvent;
import com.dave.Main.State.Observe.Observer;
import com.dave.Main.State.State;
import com.dave.Vaadin.Layout.ViewToolbar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("chargePoints")
@PageTitle("Charge Points")
@Menu(order = 1, icon = "vaadin:connect", title = "ChargePoints")
public class ChargePointView extends VerticalLayout implements Observer<ChargePointEvent> {
    private UI ui;
    private final State state;

    private final Grid<ChargePoint> chargePointGrid;

    @Autowired
    public ChargePointView(State state) {
        this.state = state;
        this.state.subscribe(this, ChargePointEvent.class);

        add(new ViewToolbar("Connected Charge Points"));

        this.chargePointGrid = new Grid<>(ChargePoint.class, false);
        chargePointGrid.addColumn(ChargePoint::getIpAddress).setHeader("IP Address");
        chargePointGrid.addColumn(ChargePoint::getVendor).setHeader("Vendor");
        chargePointGrid.addColumn(cp -> cp.getStatus() != null ? cp.getStatus().getStatus().getValue() : "")
                .setHeader("Status")
                .setTooltipGenerator(cp -> cp.getStatus() != null ? cp.getStatus().getStatus().getCondition() : "");
        chargePointGrid.addColumn(ChargePoint::getLastHeartbeat).setHeader("Heartbeat");
        chargePointGrid.addColumn(ChargePoint::getConnectors).setHeader("Connectors");
        add(this.chargePointGrid);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        updateView();
    }

    private void updateView() {
        chargePointGrid.setItems(this.state.getChargePoints());
    }

    @Override
    public void onNotify(ChargePointEvent event) {
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
