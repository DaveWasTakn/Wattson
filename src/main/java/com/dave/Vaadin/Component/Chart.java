package com.dave.Vaadin.Component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

import java.time.Instant;
import java.util.Map;

@Tag("chartjs-chart")
@JsModule("./components/chartjs-chart.ts")
@NpmPackage(value = "chart.js", version = "4.5.1")
@NpmPackage(value = "chartjs-adapter-moment", version = "1.0.1")
public class Chart extends Component implements HasSize {

    public void addDataPoint(int datasetIndex, Instant timestamp, Number dataPoint) {
        getElement().callJsFunction("addDataPoint", datasetIndex, timestamp, dataPoint);
    }

    public void setDatasetLabel(int datasetIndex, String label) {
        getElement().callJsFunction("setDatasetLabel", datasetIndex, label);
    }

    public void updateOptionsMap(Map<String, ?> newProperties) {
        getElement().callJsFunction("updateOptionsMap", newProperties);
    }

    public void updateDataMap(Map<String, ?> newProperties) {
        getElement().callJsFunction("updateDataMap", newProperties);
    }

    public void addDataset(Map<String, ?> dataset) {
        getElement().callJsFunction("addDataset", dataset);
    }

    public void initChart(Map<String, Object> config) {
        getElement().callJsFunction("initChart", config);
    }
}