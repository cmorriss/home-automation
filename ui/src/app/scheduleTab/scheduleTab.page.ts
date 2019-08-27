import {Component, QueryList, ViewChildren} from '@angular/core';
import {IotService} from '../iot.service';
import {Events, IonSelect, LoadingController, PickerController, PopoverController} from '@ionic/angular';
import {Schedule} from '../models/Schedule';
import {SwitchView} from '../models/SwitchView';
import {DatePickerComponent} from './date-picker.component';
import {ScheduleStatusView} from '../models/ScheduleStatusView';
import {ScheduleStatusEnum} from '../models/ScheduleStatusEnum';
import {DatePipe} from '@angular/common';

@Component({
    selector: 'app-tab2',
    templateUrl: 'scheduleTab.page.html',
    styleUrls: ['scheduleTab.page.scss']
})
export class ScheduleTabPage {

    public scheduleStatus: ScheduleStatusView = new ScheduleStatusView(ScheduleStatusEnum.Loading, new Date());

    switches: any;

    public static dayViewMap: Map<string, string> = new Map([
        ['MONDAY', 'Mon'],
        ['TUESDAY', 'Tues'],
        ['WEDNESDAY', 'Wed'],
        ['THURSDAY', 'Thurs'],
        ['FRIDAY', 'Fri'],
        ['SATURDAY', 'Sat'],
        ['SUNDAY', 'Sun']
    ]);

    @ViewChildren(IonSelect) selectGroup: QueryList<IonSelect>;

    constructor(
        public iotService: IotService,
        public loadingController: LoadingController,
        public pickerCtrl: PickerController,
        public popoverController: PopoverController,
        public events: Events,
        public datePipe: DatePipe
    ) {
        events.subscribe('PausedUntilDate', (data) => {
            this.scheduleStatus.pausedUntilDate = data;
            this.updateScheduleStatus(this.scheduleStatus);
        })
    }

    async getSwitches() {
        const loading = await this.loadingController.create({
            message: 'Loading'
        });
        await loading.present();
        await this.iotService.getSwitches()
            .subscribe(res => {
                console.log(res);
                this.switches = res;
                loading.dismiss();
            }, err => {
                console.log(err);
                loading.dismiss();
            });
    }

    async getScheduleStatus() {
        await this.iotService.getScheduleStatus()
            .subscribe(res => {
                console.log(res);
                this.scheduleStatus = new ScheduleStatusView(ScheduleStatusEnum.parseEnum(res.status), new Date(res.pausedUntilDate));
            });
    }

    async presentPopover(ev: any) {
        const popover = await this.popoverController.create({
            component: DatePickerComponent,
            componentProps: {'date': this.scheduleStatus.pausedUntilDate},
            event: ev,
            translucent: true
        });
        return await popover.present();
    }

    ngOnInit() {
        console.log("Initializing the schedule view");
        this.getSwitches();
        this.getScheduleStatus();
    }


    public pausedUntilDateString(): string {
        return this.scheduleStatus.pausedUntilDate.toLocaleDateString()
    }

    public buildDays(schedule: Schedule): string {
        var days = '';
        schedule.daysOn.map(day => {
            return ScheduleTabPage.dayViewMap.get(day);
        }).forEach(day => {
            days = ScheduleTabPage.addDay(days, day);
        });
        return days;
    }

    public updateDays(schedule: Schedule, event: CustomEvent<any>) {
        schedule.daysOn = event.detail.value;
        this.updateSchedule(schedule);
    }

    private static addDay(days: string, day: string): string {
        if (days.length == 0) {
            return day;
        } else {
            return days + ',' + day;
        }
    }

    public updateSchedule(schedule: Schedule) {
        this.iotService.updateSchedule(schedule).subscribe(
            response => console.log(response),
            err => console.log(err)
        );
    }

    public updateScheduleStatus(status: ScheduleStatusView) {
        this.iotService.updateScheduleStatus(status).subscribe(
            response => console.log(response),
            err => console.log(err)
        );
    }

    public isPaused(): boolean {
        return false;
    }

    async openScheduleStatusPicker() {
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
                            value: ScheduleStatusEnum.Active
                        },
                        {
                            text: 'Paused',
                            value: ScheduleStatusEnum.Paused
                        },
                        {
                            text: 'Stopped',
                            value: ScheduleStatusEnum.Stopped
                        }
                    ]
                }
            ]
        });
        let selectedIndex = 0;
        if (this.scheduleStatus.status == ScheduleStatusEnum.Paused) {
            selectedIndex = 1;
        } else if (this.scheduleStatus.status == ScheduleStatusEnum.Stopped) {
            selectedIndex = 2;
        }

        picker.columns[0].selectedIndex = selectedIndex;
        await picker.present();
        picker.onDidDismiss().then(async data => {
            let col = await picker.getColumn('Manage Schedule');
            this.scheduleStatus = new ScheduleStatusView(col.options[col.selectedIndex].value, new Date());
            this.updateScheduleStatus(this.scheduleStatus);
        });
    }

    async openStartTimePicker(schedule: Schedule) {
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

        let timeComponents = schedule.startTime.split(':');
        let currentHour = parseInt(timeComponents[0]);
        let currentMinute = parseInt(timeComponents[1]) / 10;
        picker.columns[0].selectedIndex = currentHour;
        picker.columns[1].selectedIndex = currentMinute;
        await picker.present();
        picker.onDidDismiss().then(async data => {
            let hourCol = await picker.getColumn('Hour');
            let minuteCol = await picker.getColumn('Minute');
            let hour = hourCol.options[hourCol.selectedIndex].text;
            let minute = minuteCol.options[minuteCol.selectedIndex].text;
            schedule.startTime = hour + ':' + minute;
            this.updateSchedule(schedule);
        });
    }

    async openDurationPicker(schedule: Schedule) {
        const picker = await this.pickerCtrl.create({
            buttons: [{
                text: 'Done',
            }],
            columns: [
                {
                    name: 'Duration',
                    options: [
                        {
                            text: '05',
                            value: 5
                        },
                        {
                            text: '10',
                            value: 10
                        },
                        {
                            text: '15',
                            value: 15
                        },
                        {
                            text: '20',
                            value: 20
                        },
                        {
                            text: '25',
                            value: 25
                        },
                        {
                            text: '30',
                            value: 30
                        },
                        {
                            text: '35',
                            value: 35
                        },
                        {
                            text: '40',
                            value: 40
                        },
                        {
                            text: '45',
                            value: 45
                        },
                        {
                            text: '50',
                            value: 50
                        },
                        {
                            text: '55',
                            value: 55
                        },
                        {
                            text: '60',
                            value: 60
                        }
                    ]
                }
            ]
        });

        picker.columns[0].selectedIndex = (schedule.duration / 5) - 1;

        await picker.present();
        picker.onDidDismiss().then(async data => {
            let durationCol = await picker.getColumn('Duration');
            schedule.duration = parseInt(durationCol.options[durationCol.selectedIndex].text);
            this.updateSchedule(schedule);
        });
    }

    openDaysSelect(aSwitch: SwitchView) {
        this.selectGroup.forEach((item, index, ionSelect) => {
            if (item.placeholder == (aSwitch.name + 'select')) {
                item.value = aSwitch.schedule.daysOn;
                item.open();
            }
        });
    }
}
