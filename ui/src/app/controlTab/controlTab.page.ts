import {Component} from '@angular/core';
import {IotService} from '../iot.service';
import {LoadingController} from '@ionic/angular';
import {ControlView} from '../viewModels/ControlView';
import {ControlGroup} from '../dataModels/ControlGroup';
import {ControlGroupView} from '../viewModels/ControlGroupView';

@Component({
    selector: 'app-tab1',
    templateUrl: 'controlTab.page.html',
    styleUrls: ['controlTab.page.scss']
})
export class ControlTabPage {

    public controlGroups: ControlGroupView[] = [];

    constructor(public iotService: IotService, public loadingController: LoadingController) {
    }

    async getControlGroups() {
        const loading = await this.loadingController.create({
            message: 'Loading'
        });
        await loading.present();
        await this.iotService.getControlGroups()
            .subscribe(res => {
                console.log('Found these control groups:');
                console.log(res);
                this.controlGroups = Array.from<ControlGroup>(res).map(cg => new ControlGroupView(cg));
                loading.dismiss();
            }, err => {
                console.log(err);
                loading.dismiss();
            });
    }

    public toggle(control: ControlView) {
        control.toggle();
        this.iotService.updateControl(control.toControl());
    }

    ionViewWillEnter() {
        this.getControlGroups();
    }
}
