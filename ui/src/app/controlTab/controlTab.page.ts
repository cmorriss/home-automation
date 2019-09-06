import {Component} from '@angular/core';
import {IotService} from '../iot.service';
import {LoadingController} from '@ionic/angular';
import {SwitchView} from '../models/SwitchView';
import {Schedule} from '../models/Schedule';
import {SwitchGroup} from "../models/SwitchGroup";

@Component({
    selector: 'app-tab1',
    templateUrl: 'controlTab.page.html',
    styleUrls: ['controlTab.page.scss']
})
export class ControlTabPage {

    switchGroups: SwitchGroup[] = [];

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
                var foundSwitchGroups = SwitchGroup.fromSwitches(res).values();
                console.log("Found these switch groups:");
                console.log(foundSwitchGroups);
                this.switchGroups = Array.from(foundSwitchGroups);
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
