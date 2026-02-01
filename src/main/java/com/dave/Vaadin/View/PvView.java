package com.dave.Vaadin.View;

import com.dave.Main.Pv.PvSystem;
import com.dave.Main.State.Observe.Observer;
import com.dave.Main.State.Observe.Event.PvSystemEvent;
import com.dave.Main.State.State;
import com.dave.Vaadin.Layout.ViewToolbar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;

import static com.dave.Main.Util.Utils.formatEpochSeconds;

@Route("")
@RouteAlias("pv")
@PageTitle("Photovoltaic System")
@Menu(order = 0, icon = "vaadin:bolt", title = "Photovoltaic System")
public class PvView extends VerticalLayout implements Observer<PvSystemEvent> {
    private UI ui;
    private final State state;

    private final PvSystem pvSystem;
    private final Span production;
    private final Span consumption;
    private final Span battery;
    private final Span grid;
    private final Span lastUpdate;


    @Autowired
    public PvView(State state, PvSystem pvSystem) {
        this.state = state;
        this.pvSystem = pvSystem;

        this.state.subscribe(this, PvSystemEvent.class);

        add(new ViewToolbar("Photovoltaic System"));

        FormLayout form = new FormLayout();

        this.production = new Span();
        this.consumption = new Span();
        this.battery = new Span();
        this.grid = new Span();
        this.lastUpdate = new Span();

        form.addFormItem(this.production, "Production");
        form.addFormItem(this.consumption, "Consumption");
        form.addFormItem(this.battery, "Battery");
        form.addFormItem(this.grid, "Grid");
        form.addFormItem(this.lastUpdate, "Last Update");

        form.setWidth("20em");
//        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP));

        Div formWrapper = new Div(form);
        formWrapper.setWidthFull();
        formWrapper.getStyle().set("display", "flex");
        formWrapper.getStyle().set("justify-content", "center");

        add(formWrapper);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    private void updateView() {
        this.production.setText(this.pvSystem.getCurrentWattProduction() + " W");
        this.consumption.setText(this.pvSystem.getCurrentWattConsumption() + " W");
        this.battery.setText(this.pvSystem.getBatteryWattPower() + " W (" + this.pvSystem.getBatterySocPercent() + "%)");
        this.grid.setText(this.pvSystem.getGridWattPower() + " W");
        this.lastUpdate.setText(formatEpochSeconds(this.pvSystem.getLastMeterUpdateEpoch()));
    }


    @Override
    public void onNotify(PvSystemEvent event) {
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
