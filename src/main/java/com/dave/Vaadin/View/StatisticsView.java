package com.dave.Vaadin.View;

import com.dave.Main.Pv.PvSystem;
import com.dave.Main.State.Observe.Event.PvSystemStatusUpdateEvent;
import com.dave.Main.State.Observe.Observer;
import com.dave.Main.State.State;
import com.dave.Vaadin.Component.Chart;
import com.dave.Vaadin.Layout.ViewToolbar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Route("statistics")
@PageTitle("Statistics")
@Menu(order = 0, icon = "vaadin:chart", title = "Statistics")
public class StatisticsView extends VerticalLayout implements Observer<PvSystemStatusUpdateEvent> {
    private UI ui;
    private final State state;
    private final PvSystem pvSystem;

    private final Chart chart;
    private final Map<Integer, Supplier<Number>> datasetOrderMap = new HashMap<>();

    @Autowired
    public StatisticsView(State state, PvSystem pvSystem) {
        this.pvSystem = pvSystem;
        this.state = state;
        this.state.subscribe(this, PvSystemStatusUpdateEvent.class);

        add(new ViewToolbar("Statistics"));

        this.chart = new Chart();
        this.chart.setChartSlidingWindow(1_000 * 60 * 15);
        this.chart.initChart(Map.of(
                "type", "line",
                "data", Map.of(
                        "datasets", List.of(
                                Map.of("label", "Consumption", "data", List.of(), "pointRadius", 0, "pointHoverRadius", 4),
                                Map.of("label", "Production", "data", List.of(), "pointRadius", 0, "pointHoverRadius", 4),
                                Map.of("label", "Grid Power", "data", List.of(), "pointRadius", 0, "pointHoverRadius", 4),
                                Map.of("label", "Battery Power", "data", List.of(), "pointRadius", 0, "pointHoverRadius", 4)
                        )
                ),
                "options", Map.of(
                        "normalized", true,
                        "animation", false,
                        "responsive", true,
                        "maintainAspectRatio", false,
                        "scales", Map.of(
                                "x", Map.of(
                                        "type", "timestack",
                                        "title", Map.of(
                                                "display", true,
                                                "text", "Time"
                                        )
                                ),
                                "y", Map.of(
                                        "title", Map.of(
                                                "display", true,
                                                "text", "Watt"
                                        )
                                )
                        ),
                        "interaction", Map.of(
                                "mode", "nearest",
                                "intersect", false
                        )
                )
        ));
        this.datasetOrderMap.put(0, this.pvSystem::getCurrentWattConsumption); // TODO change if event provides actual PvMeasurement !!!!!!!!!!!!!!!!!!!!!!!!
        this.datasetOrderMap.put(1, this.pvSystem::getCurrentWattProduction);
        this.datasetOrderMap.put(2, this.pvSystem::getGridWattPower);
        this.datasetOrderMap.put(3, this.pvSystem::getBatteryWattPower);

        this.chart.getElement().getStyle().set("width", "100%");
        this.chart.getElement().getStyle().set("height", "50%");

        add(this.chart);
    }

    private void updateChart() {
        Instant now = Instant.now();  // TODO just provide the actual update entity instead of this stupid injection of the pvSystem so to get the values and timestamp directly from a pvmeasurement !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        this.datasetOrderMap.forEach((k, v) -> this.chart.addDataPoint(k, now, v.get())); // TODO change
    }


    @Override
    public void onNotify(PvSystemStatusUpdateEvent event) {
        if (this.ui != null) {
            this.ui.access(this::updateChart);
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
