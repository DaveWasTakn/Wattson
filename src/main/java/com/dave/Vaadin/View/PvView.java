package com.dave.Vaadin.View;

import com.dave.Main.State.Observe.Observer;
import com.dave.Main.State.Observe.PvSystemEvent;
import com.dave.Main.State.State;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("/pv")
@PageTitle("Photovoltaic System")
@Menu(order = 1, icon = "vaadin:bolt", title = "Photovoltaic System")
public class PvView extends VerticalLayout implements Observer<PvSystemEvent> {

    private final State state;

    private UI ui;

    public PvView(State state) {
        this.state = state;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        this.ui = attachEvent.getUI();
        super.onAttach(attachEvent);
    }

    @Override
    public void onNotify(PvSystemEvent event) {
        // TODO
    }
}
