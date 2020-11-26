import {Component, QueryList, ViewChildren} from '@angular/core';
import {IotService} from '../iot.service';
import {IonSelect, LoadingController, PickerController, PopoverController} from '@ionic/angular';
import {DatePickerComponent} from './date-picker.component';
import {AutomationStatusEnum} from '../viewModels/AutomationStatusEnum';
import {DatePipe} from '@angular/common';
import {ScheduledAutomationView} from '../viewModels/ScheduledAutomationView';
import {AutomationView} from '../viewModels/AutomationView';
import {TimeView} from '../viewModels/TimeView';
import {AutomationContainer} from '../viewModels/AutomationContainer';
import {AutomationGroupView} from '../viewModels/AutomationGroupView';
import {AutomationGroup} from '../dataModels/AutomationGroup';
import {Event} from '../dataModels/Event';
import {Action} from '../dataModels/Action';
import {Events} from '../events';

@Component({
    selector: 'app-tab2',
    templateUrl: 'automationTab.page.html',
    styleUrls: ['automationTab.page.scss']
})
export class AutomationTabPage {


    constructor(
        public iotService: IotService,
        public loadingController: LoadingController,
        public pickerCtrl: PickerController,
        public popoverController: PopoverController,
        public datePipe: DatePipe,
        events: Events
    ) {
        events.subscribe('AutomationGroupViewUpdated', (data) => {
            this.updateAutomationGroup(data);
        });
    }

    public static dayViewMap: Map<string, { short: string, order: number }> = new Map([
        ['SUNDAY', {short: 'S', order: 0}],
        ['MONDAY', {short: 'M', order: 1}],
        ['TUESDAY', {short: 'T', order: 2}],
        ['WEDNESDAY', {short: 'W', order: 3}],
        ['THURSDAY', {short: 'Th', order: 4}],
        ['FRIDAY', {short: 'F', order: 5}],
        ['SATURDAY', {short: 'Sa', order: 6}]
    ]);

    @ViewChildren(IonSelect) selectGroup: QueryList<IonSelect>;

    automationGroups: AutomationGroupView[];
    events: Event[];
    actions: Action[];

    private static addDay(days: string, day: string): string {
        if (days.length === 0) {
            return day;
        } else {
            return days + ',' + day;
        }
    }

    controlKindDurationOptions(): any {
        return [
            {
                text: '05',
                value: 5
            },
            {
                text: '10',
                value:
                    10
            },
            {
                text: '15',
                value:
                    15
            },
            {
                text: '20',
                value:
                    20
            },
            {
                text: '25',
                value:
                    25
            },
            {
                text: '30',
                value:
                    30
            },
            {
                text: '35',
                value:
                    35
            },
            {
                text: '40',
                value:
                    40
            },
            {
                text: '45',
                value:
                    45
            },
            {
                text: '50',
                value:
                    50
            },
            {
                text: '55',
                value:
                    55
            },
            {
                text: '60',
                value:
                    60
            }
        ];
    }
    async getAutomationGroups() {
        const loading = await this.loadingController.create({
            message: 'Loading'
        });
        await loading.present();
        console.log('Starting events load...');
        this.events = await this.iotService.getEvents().toPromise();
        console.log('Starting actions load...');
        this.actions = await this.iotService.getActions().toPromise();
        console.log('received events and actions promises. starting load of automation groups...');
        await this.iotService.getAutomationGroups()
            .subscribe(res => {
                console.log('Loaded automation groups...');
                console.log(res);
                const rawGroups: AutomationGroup[] = Array.from(res);

                this.automationGroups = rawGroups.map(
                    ag => {
                        console.log('Creating automation group view...');
                        return new AutomationGroupView(ag, this.events, this.actions);
                    }
                );
                loading.dismiss();
            }, err => {
                console.log(err);
                loading.dismiss();
            });
    }


    async openResumeDatePicker(ev: any, automationGroup: AutomationGroupView) {
        const popover = await this.popoverController.create({
            component: DatePickerComponent,
            componentProps: {automationGroup},
            event: ev,
            translucent: true
        });
        await popover.present();
    }

    ionViewWillEnter() {
        this.getAutomationGroups();
    }

    public buildDays(schedule: ScheduledAutomationView): string {
        let days = '';
        schedule.daysOfTheWeek.map(day => {
            return AutomationTabPage.dayViewMap.get(day);
        }).sort((day1, day2) => {
            return day1.order < day2.order ? -1 : day1.order > day2.order ? 1 : 0;
        }).forEach(day => {
            days = AutomationTabPage.addDay(days, day.short);
        });
        return days;
    }

    public buildDurationString(schedule: ScheduledAutomationView): string {
        if (schedule.duration < 60) {
            return schedule.duration.toString() + ' min';
        } else {
            return (schedule.duration / 60).toString() + ' hr';
        }
    }

    public updateDays(schedule: ScheduledAutomationView, event: CustomEvent<any>) {
        schedule.daysOfTheWeek = event.detail.value;
        this.updateAutomation(schedule);
    }

    public updateAutomation(automationView: AutomationView) {
        automationView.toAutomations().forEach(container => {
            this.updateAutomationContainer(container);
        });
    }

    private updateAutomationContainer(automationContainer: AutomationContainer) {
        this.iotService.updateAutomation(automationContainer.automation);
        this.iotService.updateEvent(automationContainer.event);
        this.iotService.updateAction(automationContainer.action);
    }

    public updateAutomationGroup(automationGroupView: AutomationGroupView) {
        this.iotService.updateAutomationGroup(automationGroupView.toAutomationGroup().automationGroup);
    }

    async openScheduleStatusPicker(automationGroup: AutomationGroupView) {
        const picker = await this.pickerCtrl.create({
            buttons: [{
                text: 'Done',
            }],
            columns: [
                {
                    name: 'Manage Schedule',
                    options: [
                        {
                            text: 'Active',
                            value: AutomationStatusEnum.Active.value
                        },
                        {
                            text: 'Paused',
                            value: AutomationStatusEnum.Paused.value
                        },
                        {
                            text: 'Stopped',
                            value: AutomationStatusEnum.Stopped.value
                        }
                    ]
                }
            ]
        });
        let selectedIndex = 0;
        if (automationGroup.status === AutomationStatusEnum.Paused) {
            selectedIndex = 1;
        } else if (automationGroup.status === AutomationStatusEnum.Stopped) {
            selectedIndex = 2;
        }

        picker.columns[0].selectedIndex = selectedIndex;
        await picker.present();
        picker.onDidDismiss().then(async data => {
            const col = await picker.getColumn('Manage Schedule');
            automationGroup.status = AutomationStatusEnum.parseEnum(col.options[col.selectedIndex].value);
            automationGroup.resumeDate = new Date();
            this.updateAutomationGroup(automationGroup);
        });
    }

    async openStartTimePicker(automation: AutomationView, time: TimeView) {
        const picker = await this.pickerCtrl.create({
            buttons: [{
                text: 'Done',
            }],
            columns: [
                {
                    name: 'Hour',
                    options: [
                        {
                            text: '00',
                            value: '00'
                        },
                        {
                            text: '01',
                            value: '01'
                        },
                        {
                            text: '02',
                            value: '02'
                        },
                        {
                            text: '03',
                            value: '03'
                        },
                        {
                            text: '04',
                            value: '04'
                        },
                        {
                            text: '05',
                            value: '05'
                        },
                        {
                            text: '06',
                            value: '06'
                        },
                        {
                            text: '07',
                            value: '07'
                        },
                        {
                            text: '08',
                            value: '08'
                        },
                        {
                            text: '09',
                            value: '09'
                        },
                        {
                            text: '10',
                            value: '10'
                        },
                        {
                            text: '11',
                            value: '11'
                        },
                        {
                            text: '12',
                            value: '12'
                        },
                        {
                            text: '13',
                            value: '13'
                        },
                        {
                            text: '14',
                            value: '14'
                        },
                        {
                            text: '15',
                            value: '15'
                        },
                        {
                            text: '16',
                            value: '16'
                        },
                        {
                            text: '17',
                            value: '17'
                        },
                        {
                            text: '18',
                            value: '18'
                        },
                        {
                            text: '19',
                            value: '19'
                        },
                        {
                            text: '20',
                            value: '20'
                        },
                        {
                            text: '21',
                            value: '21'
                        },
                        {
                            text: '22',
                            value: '22'
                        },
                        {
                            text: '23',
                            value: '23'
                        }
                    ]
                },
                {
                    name: 'Minute',
                    options: [
                        {
                            text: '00',
                            value: '00'
                        },
                        {
                            text: '10',
                            value: '10'
                        },
                        {
                            text: '20',
                            value: '20'
                        },
                        {
                            text: '30',
                            value: '30'
                        },
                        {
                            text: '40',
                            value: '40'
                        },
                        {
                            text: '50',
                            value: '50'
                        },
                    ]
                },
            ],

        });

        // tslint:disable-next-line:radix
        const currentHour = parseInt(time.hour);
        // tslint:disable-next-line:radix
        const currentMinute = parseInt(time.minute) / 10;
        picker.columns[0].selectedIndex = currentHour;
        picker.columns[1].selectedIndex = currentMinute;
        await picker.present();
        picker.onDidDismiss().then(async data => {
            const hourCol = await picker.getColumn('Hour');
            const minuteCol = await picker.getColumn('Minute');
            time.hour = hourCol.options[hourCol.selectedIndex].text;
            time.minute = minuteCol.options[minuteCol.selectedIndex].text;
            this.updateAutomation(automation);
        });
    }

    async openDurationPicker(schedule: ScheduledAutomationView) {
        const durationOptions = this.controlKindDurationOptions();

        const picker = await this.pickerCtrl.create({
            buttons: [{
                text: 'Done',
            }],
            columns: [{
                name: 'Duration',
                options: durationOptions
            }]
        });

        let existingIndex = durationOptions.findIndex(value => {
            return value.value === schedule.duration;
        });
        if (existingIndex === -1) {
            existingIndex = 0;
        }
        picker.columns[0].selectedIndex = existingIndex;

        await picker.present();
        picker.onDidDismiss().then(async data => {
            const durationCol = await picker.getColumn('Duration');
            schedule.duration = durationCol.options[durationCol.selectedIndex].value;
            this.updateAutomation(schedule);
        });
    }

    openDaysSelect(automation: ScheduledAutomationView) {
        this.selectGroup.forEach((item, index, ionSelect) => {
            if (item.placeholder === (automation.name + 'select')) {
                item.value = automation.daysOfTheWeek;
                item.open();
            }
        });
    }
}
