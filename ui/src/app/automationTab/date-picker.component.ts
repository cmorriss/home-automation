import {Component, OnInit} from '@angular/core';
import {NavParams, PopoverController} from '@ionic/angular';
import {AutomationGroupView} from '../viewModels/AutomationGroupView';
import {Events} from '../events';

@Component({
    selector: 'app-date-picker',
    templateUrl: './date-picker.component.html',
    styleUrls: ['./date-picker.component.scss'],
})
export class DatePickerComponent implements OnInit {
    public resumeDate: Date;
    public automationGroup: AutomationGroupView;

    constructor(private navParams: NavParams, private events: Events, private popoverController: PopoverController) {
        this.automationGroup = navParams.get('automationGroup');
        this.resumeDate = this.automationGroup.resumeDate;
    }

    ngOnInit() {
    }

    public resumeDateSelected(selectedDate: Date) {
        this.resumeDate = selectedDate;
        console.log('selected resume date:');
        console.log(selectedDate);
        this.automationGroup.resumeDate = this.resumeDate;
        this.events.publish('AutomationGroupViewUpdated', this.automationGroup)
        this.popoverController.dismiss();
    }

    async DismissClick() {
        await this.popoverController.dismiss();
    }
}
