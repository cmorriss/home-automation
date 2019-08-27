import {Component, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {IotService} from '../iot.service';
import {LoadingController, PickerController, IonSelect} from '@ionic/angular';
import {SwitchView} from '../models/SwitchView';
import {Schedule} from '../models/Schedule';

@Component({
    selector: 'app-tab1',
    templateUrl: 'controlTab.page.html',
    styleUrls: ['controlTab.page.scss']
})
export class ControlTabPage {

    switches: SwitchView[];

    constructor(public iotService: IotService, public loadingController: LoadingController) {
    }

    async getSwitches() {
        const loading = await this.loadingController.create({
            message: 'Loading'
        });
        await loading.present();
        await this.iotService.getSwitches()
            .subscribe(res => {
                console.log(res);
                this.switches = res.map(aSwitch => new SwitchView(aSwitch));
                loading.dismiss();
            }, err => {
                console.log(err);
                loading.dismiss();
            });
    }

    public updateStatus() {

    }

    public toggleSwitch(aSwitch: SwitchView) {
        aSwitch.toggle();
        this.iotService.updateSwitch(aSwitch.toSwitch()).subscribe(
            response => console.log(response),
            err => console.log(err)
        );
    }

    public updateSchedule(schedule: Schedule) {
        this.iotService.updateSchedule(schedule).subscribe(
            response => console.log(response),
            err => console.log(err)
        );
    }

    ionViewWillEnter() {
        this.getSwitches();
    }
}
