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
    @ViewChild('soilMoistureCanvas') soilMoistureCanvas: ElementRef;

    metrics: Metric[];
    humidityChartData: MetricData;
    humidityChart: any;

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
        await this.iotService.getMetricData(humidityMetric)
            .subscribe(res => {
                this.humidityChartData = res;
                loading.dismiss();
                this.buildHumidityChart();
            }, err => {
                console.log(err);
                loading.dismiss();
            });
    }

    public buildHumidityChart() {
        const data = this.humidityChartData.values
        this.humidityChart = new Chart(this.humidityCanvas.nativeElement, {
            type: 'line',
            data: {
                labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'November', 'December'],
                datasets: [
                    {
                        label: 'Percent Humidity',
                        fill: false,
                        lineTension: 0.1,
                        backgroundColor: 'rgba(75,192,192,0.4)',
                        borderColor: 'rgba(75,192,192,1)',
                        borderCapStyle: 'butt',
                        borderDash: [],
                        borderDashOffset: 0.0,
                        borderJoinStyle: 'miter',
                        pointBorderColor: 'rgba(75,192,192,1)',
                        pointBackgroundColor: '#fff',
                        pointBorderWidth: 1,
                        pointHoverRadius: 5,
                        pointHoverBackgroundColor: 'rgba(75,192,192,1)',
                        pointHoverBorderColor: 'rgba(220,220,220,1)',
                        pointHoverBorderWidth: 2,
                        pointRadius: 1,
                        pointHitRadius: 10,
                        data, // [65, 59, 80, 81, 56, 55, 40, 10, 5, 50, 10, 15],
                        spanGaps: false,
                    }
                ]
            }
        });
    }
}
