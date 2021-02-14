import {Component, ElementRef, ViewChild} from '@angular/core';
import {IotService} from '../iot.service';
import {LoadingController} from '@ionic/angular';
import {Chart} from 'chart.js';
import {Metric} from '../dataModels/Metric';
import {MetricData} from '../dataModels/MetricData';

@Component({
    selector: 'app-tab3',
    templateUrl: 'analyticsTab.page.html',
    styleUrls: ['analyticsTab.page.scss']
})
export class AnalyticsTabPage {
    @ViewChild('humidityCanvas') humidityCanvas: ElementRef;
    @ViewChild('soilMoistureCanvas') soilMoisture2Canvas: ElementRef;
    @ViewChild('soilMoistureCanvas2') soilMoisture3Canvas: ElementRef;

    metrics: Metric[];

    humidityChart: any;
    soilMoisture2Chart: any;
    soilMoisture3Chart: any;

    constructor(public iotService: IotService, public loadingController: LoadingController) {
    }

    ionViewWillEnter() {
        this.getChartData();
    }

    async getChartData() {
        const loading = await this.loadingController.create({
            message: 'Loading'
        });
        await loading.present();
        this.metrics = await this.iotService.getMetrics().toPromise();
        const humidityMetric = this.metrics.find((metric) => {
            return metric.externalName === 'MASTER_BATH_HUMIDITY'
        });
        const baselineMetric = this.metrics.find((metric) => {
            return metric.externalName === 'HUMIDITY_BASELINE';
        });
        const moisture2Metric = this.metrics.find((metric) => {
            return metric.externalName === 'MoistureSensor2';
        });
        const moisture3Metric = this.metrics.find((metric) => {
            return metric.externalName === 'MoistureSensor3';
        });
        this.iotService.getMetricData(humidityMetric, 'THREE_HOURS').toPromise().then(humidityData => {
            this.iotService.getMetricData(baselineMetric, 'THREE_HOURS').toPromise().then(baselineData => {
                this.buildHumidityChart(humidityData, baselineData);
            });
        });
        this.iotService.getMetricData(moisture2Metric, 'TWO_DAYS').toPromise()
            .then(soilMoistureData => {
                this.buildChart(soilMoistureData, this.soilMoisture2Canvas);
            });
        this.iotService.getMetricData(moisture3Metric, 'TWO_DAYS').toPromise()
            .then(soilMoistureData2 => {
                this.buildChart(soilMoistureData2, this.soilMoisture3Canvas);
                loading.dismiss();
            });


    }

    public buildChart(metricData: MetricData, canvas: ElementRef) {
        const labels = metricData.timestamps
        new Chart(canvas.nativeElement, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Soil Moisture',
                        data: metricData.values,
                        backgroundColor: `rgb(255,153,20)`,
                        borderColor: `rgb(255,153,20)`,
                        fill: false
                    }
                ]
            },
            options: {
                scales: {
                    yAxes: [{
                        ticks: {
                            min: 0,
                            max: 100
                        }
                    }]
                }
            }

        });
    }

    public buildHumidityChart(humidityChartData: MetricData, baselineData: MetricData) {
        const labels = humidityChartData.timestamps
        this.humidityChart = new Chart(this.humidityCanvas.nativeElement, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Percent Humidity',
                        fill: false,
                        lineTension: 0.1,
                        backgroundColor: 'rgba(8,189,189,0.4)',
                        borderColor: 'rgba(8,189,189,1)',
                        borderCapStyle: 'butt',
                        borderDash: [],
                        borderDashOffset: 0.0,
                        borderJoinStyle: 'miter',
                        pointBorderColor: 'rgb(8,189,189)',
                        pointBackgroundColor: '#fff',
                        pointBorderWidth: 1,
                        pointHoverRadius: 5,
                        pointHoverBackgroundColor: 'rgba(8,189,189,1)',
                        pointHoverBorderColor: 'rgba(220,220,220,1)',
                        pointHoverBorderWidth: 2,
                        pointRadius: 1,
                        pointHitRadius: 10,
                        data: humidityChartData.values,
                        spanGaps: false,
                    },
                    {
                        label: 'Humidity Baseline',
                        fill: false,
                        lineTension: 0.1,
                        backgroundColor: 'rgb(240,245,238)',
                        borderColor: 'rgba(41,191,18,1)',
                        borderCapStyle: 'butt',
                        borderDash: [],
                        borderDashOffset: 0.0,
                        borderJoinStyle: 'miter',
                        pointBorderColor: 'rgba(41,191,18,1)',
                        pointBackgroundColor: '#fff',
                        pointBorderWidth: 1,
                        pointHoverRadius: 5,
                        pointHoverBackgroundColor: 'rgba(41,191,18,1)',
                        pointHoverBorderColor: 'rgba(220,220,220,1)',
                        pointHoverBorderWidth: 2,
                        pointRadius: 1,
                        pointHitRadius: 10,
                        data: baselineData.values,
                        spanGaps: true,
                    }
                ]
            },
            options: {
                scales: {
                    yAxes: [{
                        ticks: {
                            min: 0,
                            max: 100
                        }
                    }]
                }
            }
        });
    }
}
