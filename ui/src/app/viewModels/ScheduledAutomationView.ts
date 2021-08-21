import {ControlType} from '../dataModels/Control';
import {TimeView} from './TimeView';
import {ActionType, Automation, EventType} from '../dataModels/Automation';
import {AutomationView} from './AutomationView';
import {Action} from '../dataModels/Action';
import {AutomationContainer} from './AutomationContainer';
import {AutomationStatusEnum} from './AutomationStatusEnum';
import {ControlAction} from '../dataModels/ControlAction';

export class ScheduledAutomationView implements AutomationView {
    constructor(
        private readonly existingStartAutomation: Automation,
        private readonly existingStartAction: Action,
        private readonly existingStopAutomation: Automation,
        private readonly existingStopAction: Action
    ) {
        this.id = existingStartAutomation.id;
        this.action = existingStartAction;
        // Will eventually make this more generic. May be part of a broader config driven concept of a view.
        this.useDuration = this.action.type.valueOf() === ActionType.CONTROL.valueOf()
            && (this.action as ControlAction).control.type.valueOf() === ControlType.IRRIGATION_VALVE.valueOf();
        this.name = this.action.name;
        this.startTime = new TimeView(existingStartAutomation.time);
        this.stopTime = new TimeView(this.existingStopAutomation.time);
        this.daysOfTheWeek = existingStartAutomation.daysOfTheWeek;
        this.duration = ScheduledAutomationView.calcDuration(this.startTime, this.stopTime);
        this.status = existingStartAutomation.status;
        this.resumeDate = new Date(existingStartAutomation.resumeDate);
    }

    public id: number;
    public action: Action;
    public name: string;
    public startTime: TimeView;
    public stopTime: TimeView;
    public duration: number;
    public daysOfTheWeek: string[];
    public status: AutomationStatusEnum;
    public resumeDate: Date;
    public readonly useDuration: boolean;

    private static calcStopTime(startTime: TimeView, duration: number): string {
        console.log('calculating end time for start time: ' + startTime.getTime() + ', duration: ' + duration);
        let endMin = parseInt(startTime.minute) + duration;
        let endHour = parseInt(startTime.hour);
        if (endMin > 59) {
            console.log('end min is over 59...');
            const durationHours = Math.floor(endMin / 60);
            const durationMins = endMin % 60;
            endHour = endHour + durationHours;
            if (endHour > 23) {
                endHour = endHour - 24;
            }
            endMin = durationMins;
        }
        const result = endHour.toString() + ':' + endMin.toString();
        console.log('returning ' + result);
        return result;
    }

    private static calcDuration(startTime: TimeView, stopTime: TimeView): number {
        const startHour = parseInt(startTime.hour);
        const startMin = parseInt(startTime.minute);
        const stopHour = parseInt(stopTime.hour);
        const stopMin = parseInt(stopTime.minute);
        let hours = stopHour - startHour;
        if (hours < 0) {
            hours = 24 - startHour + stopHour;
        }
        return stopMin - startMin + (60 * hours);
    }

    public toAutomations(): AutomationContainer[] {
        const startTime = this.startTime.getTime();
        let stopTime: string;
        if (this.useDuration) {
            stopTime = ScheduledAutomationView.calcStopTime(this.startTime, this.duration);
        } else {
            stopTime = this.stopTime.getTime();
        }
        const startAutomation = this.toAutomation(this.existingStartAutomation, startTime, this.existingStartAction);
        const stopAutomation = this.toAutomation(this.existingStopAutomation, stopTime, this.existingStopAction);
        return [startAutomation, stopAutomation];
    }

    private toAutomation(existing: Automation, time: string, existingAction: Action): AutomationContainer {
        return new AutomationContainer(Object.assign(new Automation(),
            {
                id: existing.id,
                eventId: existing.eventId,
                eventType: EventType.SCHEDULE,
                actionId: existing.actionId,
                actionType: existing.actionType,
                associatedAutomationId: existing.associatedAutomationId,
                status: this.status,
                resumeDate: existing.resumeDate,
                dateTime: '', // TODO: Figure out where this can be used or remove if not needed
                daysOfTheWeek: this.daysOfTheWeek,
                time
            }),
            existingAction,
            null
        );
    }
}
