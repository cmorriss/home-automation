import {Component} from '@angular/core';
import {IotService} from '../iot.service';
import {LoadingController, ModalController, PickerController} from '@ionic/angular';
import {ControlView} from '../viewModels/ControlView';
import {ControlGroup} from '../dataModels/ControlGroup';
import {ControlGroupView} from '../viewModels/ControlGroupView';
import {AutomationGroupView} from '../viewModels/AutomationGroupView';
import {AutomationStatusEnum} from '../viewModels/AutomationStatusEnum';
import {Control} from '../dataModels/Control';
import {ControlSelectModal} from './controlSelectModal';

@Component({
    selector: 'app-tab1',
    templateUrl: 'controlTab.page.html',
    styleUrls: ['controlTab.page.scss']
})
export class ControlTabPage {

    public controlGroups: ControlGroupView[] = [];
    public controls: Control[] = [];
    public editMode: boolean = false;

    constructor(
        public iotService: IotService,
        public loadingController: LoadingController,
        public pickerCtrl: PickerController,
        public modalController: ModalController
    ) {
    }

    async getControlGroups() {
        const loading = await this.loadingController.create({
            message: 'Loading'
        });
        await loading.present();
        let cgLoaded = false;
        let cLoaded = false;
        this.iotService.getControlGroups()
            .subscribe(res => {
                console.log('Found these control groups:');
                console.log(res);
                this.controlGroups = Array.from<ControlGroup>(res).map(cg => new ControlGroupView(cg));
                cgLoaded = true;
                if (cLoaded) {
                    loading.dismiss();
                }
            }, err => {
                console.log(err);
                cgLoaded = true;
                if (cLoaded) {
                    loading.dismiss();
                }
            });
        this.iotService.getControls()
            .subscribe(res => {
                console.log('Found these controls:');
                console.log(res);
                this.controls = Array.from<Control>(res);
                cLoaded = true;
                if (cgLoaded) {
                    loading.dismiss();
                }
            }, err => {
                console.log(err);
                cLoaded = true;
                if (cgLoaded) {
                    loading.dismiss();
                }
            });
    }

    public toggle(control: ControlView) {
        control.toggle();
        this.iotService.updateControl(control.toControl()).subscribe(res => {
                console.log('Updated Control, result:');
                console.log(res);
            }, err => {
                console.log(err);
                this.reload(control);
                control.setError();
            }
        );
    }

    reload(control: ControlView) {
        this.iotService.getControl(control.id).subscribe(latestControl => {
            control.setControl(latestControl);
        });
    }

    switchToEditMode() {
        this.editMode = true;
    }

    newGroup() {
        this.controlGroups.push(new ControlGroupView());
    }

    async addControl(cg: ControlGroupView) {
        const picker = await this.pickerCtrl.create({
            buttons: [{
                text: 'Done',
            }],
            columns: [
                {
                    name: 'Add Control',
                    options: this.controls.map(c => {
                        return {
                            text: c.name,
                            value: c.id
                        }
                    })
                }
            ]
        });
        await picker.present();
        picker.onDidDismiss().then(async data => {
            const col = await picker.getColumn('Add Control');
            let addedControlId = col.options[col.selectedIndex].value as number;
            if (cg.controls.find(c => c.id == addedControlId) == undefined) {
                let c = this.controls.find(c => c.id == addedControlId);
                cg.controls.push(new ControlView(c, true));
            } else {
                console.log(`Control ${addedControlId} was not added since it already exists in the list of controls for this group.`)
            }
        });
    }

    deleteGroup(cg: ControlGroupView) {
        if (cg.isNew()) {
            this.controlGroups = this.controlGroups.filter(cgv => cgv.id != cg.id)
        } else {
            cg.delete();
        }
    }

    cancelChanges() {
        this.editMode = false;
        this.controlGroups = this.controlGroups.filter(cg => {
            if (cg.isNew()) {
                return false;
            } else {
                if (cg.isChanged()) {
                    cg.revertEdits();
                }
                return true;
            }
        });
    }

    saveChanges() {
        this.editMode = false;
        this.controlGroups = this.controlGroups.map((cg, index) => {
            let shouldReturnCg = true;
            if (cg.deleted) {
                this.iotService.deleteControlGroup(cg.toControlGroup());
                return null;
            } else if (cg.isNew()) {
                this.iotService.createControlGroup(cg.toControlGroup()).subscribe(res => {
                    let createdCg = res as ControlGroup
                    console.log('Created new control group, id:' + createdCg.id);
                    shouldReturnCg = false;
                    return new ControlGroupView(createdCg);
                }, err => {
                    console.log(err);
                });
            } else if (cg.isChanged()) {
                cg.saveChanges();
                this.iotService.updateControlGroup(cg.toControlGroup()).subscribe(res => {
                        shouldReturnCg = false;
                        return new ControlGroupView(res);
                    }, error => {
                        console.log(error);
                    }
                );
            }
            if (!shouldReturnCg) {
                console.log('Uhh, check yourself')
            }
            return cg;
        });
    }

    async presentControlSelectModal() {
        const modal = await this.modalController.create({
            component: ControlSelectModal,
            componentProps: {
                'availableControls': ,
                'lastName': 'Adams',
                'middleInitial': 'N'
            }
        });
        return await modal.present();
    }

    ionViewWillEnter() {
        this.getControlGroups();
    }
}
