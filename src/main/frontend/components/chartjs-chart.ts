import {css, html, LitElement, type TemplateResult} from 'lit';
import {customElement, property} from 'lit/decorators.js';
import {Chart, type ChartDataset, type ChartType, registerables} from 'chart.js';
import 'chartjs-adapter-moment';

Chart.register(...registerables);

type TimeSeriesPoint = { x: string | number | Date; y: number };

@customElement('chartjs-chart')
export class ChartjsChart extends LitElement {
    static styles = css`
        :host {
            display: block;
        }

        canvas {
            width: 100%;
            height: 50%;
        }
    `;

    @property({type: String})
    chartType: ChartType = 'line';

    private chart?: Chart;
    private chartConfig: any;

    protected firstUpdated(): void {
        const canvas = this.renderRoot.querySelector('canvas') as HTMLCanvasElement;

        if (this.chartConfig) {
            this.chart = new Chart(canvas, this.chartConfig);
        } else {
            this.chart = new Chart(canvas, {
                type: this.chartType,
                data: {datasets: []},
                options: {
                    responsive: true, maintainAspectRatio: false, scales: {
                        x: {type: 'time', time: {unit: 'minute'}, title: {display: true, text: 'Time'}},
                        y: {title: {display: true, text: 'Value'}}
                    }
                }
            });
        }
    }

    initChart(config: any): void {
        if (this.chart) {
            this.chart.destroy();
        }
        this.chartConfig = config;
    }


    render(): TemplateResult {
        return html`
            <canvas></canvas>`;
    }

    disconnectedCallback(): void {
        super.disconnectedCallback();
        this.chart?.destroy();
    }

    addDataPoint(datasetIndex: number, x: string | Date | number, y: number): void {
        console.log('addDataPoint', datasetIndex, x, y);
        if (!this.chart) return;

        if (!this.chart.data.datasets[datasetIndex]) {
            this.chart.data.datasets[datasetIndex] = {
                label: `Dataset ${datasetIndex}`,
                data: []
            };
        }

        const dataset = this.chart.data.datasets[datasetIndex] as ChartDataset<any, TimeSeriesPoint[]>;

        dataset.data.push({x, y});

        this.chart.update();
    }

    setDatasetLabel(datasetIndex: number, label: string): void {
        console.log(this.chart?.config);
        const dataset = this.chart?.data.datasets[datasetIndex];
        if (!dataset) return;
        dataset.label = label;
        this.chart?.update();
    }

    updateOptionsMap(newProperties: { [key: string]: any }): void {
        if (!this.chart) return;
        this.updateObject(this.chart.options, newProperties);
        console.log(this.chart.config);
        this.chart.update();
    }

    updateDataMap(newProperties: { [key: string]: any }): void {
        if (!this.chart) return;
        this.updateObject(this.chart.data, newProperties);
        console.log(this.chart.data);
        this.chart.update();
    }

    addDataset(dataset: { [key: string]: any }): void {
        if (!this.chart) return;
        this.chart.data.datasets.push(dataset as ChartDataset<any, TimeSeriesPoint[]>);
    }

    /**
     * update leaf nodes of Object ´toUpdate´ with any values present in ´newProperties´
     */
    updateObject(toUpdate: { [key: string]: any }, newProperties: { [key: string]: any }): void {
        for (let [k, v] of Object.entries(newProperties)) {
            const valueIsObject: boolean = v !== null && typeof v === 'object' && !Array.isArray(v);
            if (valueIsObject) {
                if (!toUpdate[k]) {
                    toUpdate[k] = v;
                } else {
                    this.updateObject(toUpdate[k], newProperties[k]);
                }
            } else {
                toUpdate[k] = v;
            }
        }
    }

    getDataMap(): { [key: string]: any } {
        if (!this.chart) return {};
        return this.chart.data;
    }

}

export default ChartjsChart;
