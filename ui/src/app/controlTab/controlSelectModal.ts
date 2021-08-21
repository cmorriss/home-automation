import { Component, Input } from '@angular/core';
import {ControlDef} from '../dataModels/ControlDef';

@Component({
    selector: 'control-select-modal',
    templateUrl: 'controlSelectModal.html',
    styleUrls: ['./controlSelectModal.scss']
})

export class ControlSelectModal {

    // Data passed in by componentProps
    @Input() availableControls: ControlDef[];
    @Input() lastName: string;
    @Input() middleInitial: string;

    dismiss() {
        // using the injected ModalController this page
        // can "dismiss" itself and optionally pass back data
        this.modalController.dismiss({
            'dismissed': true
        });
    }
}
