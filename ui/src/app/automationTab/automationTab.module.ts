import {IonicModule} from '@ionic/angular';
import {RouterModule} from '@angular/router';
import {NgModule} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AutomationTabPage} from './automationTab.page';
import {DatePickerComponent} from './date-picker.component';
import {DatePickerModule} from 'ionic4-date-picker';


@NgModule({
    imports: [
        IonicModule,
        CommonModule,
        FormsModule,
        RouterModule.forChild([{path: '', component: AutomationTabPage}]),
        DatePickerModule
    ],
    providers: [DatePipe],
    declarations: [AutomationTabPage, DatePickerComponent],
    entryComponents: [DatePickerComponent]
})
export class AutomationTabPageModule {
}
