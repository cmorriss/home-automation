import {IonicModule} from '@ionic/angular';
import {RouterModule} from '@angular/router';
import {NgModule} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ScheduleTabPage} from './scheduleTab.page';
import {DatePickerComponent} from './date-picker.component';
import {DatePickerModule} from 'ionic4-date-picker';


@NgModule({
    imports: [
        IonicModule,
        CommonModule,
        FormsModule,
        RouterModule.forChild([{path: '', component: ScheduleTabPage}]),
        DatePickerModule
    ],
    providers: [DatePipe],
    declarations: [ScheduleTabPage, DatePickerComponent],
    entryComponents: [DatePickerComponent]
})
export class ScheduleTabPageModule {
}
