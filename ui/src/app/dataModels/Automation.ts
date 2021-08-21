import 'reflect-metadata';
import {jsonArrayMember, jsonMember, jsonObject} from 'typedjson';
import {AutomationStatusEnum} from '../viewModels/AutomationStatusEnum';

@jsonObject
export class Automation {
    @jsonMember
    public id: number;
    @jsonMember
    public eventId: number;
    @jsonMember
    public eventType: EventType;
    @jsonMember
    public actionId: number;
    @jsonMember
    public actionType: ActionType;
    @jsonMember
    public associatedAutomationId: number;
    @jsonMember(AutomationStatusEnum.serializer)
    public status: AutomationStatusEnum;
    @jsonMember
    public resumeDate: string;
    @jsonArrayMember(String)
    public daysOfTheWeek: string[];
    @jsonMember
    public time: string;
    @jsonMember
    public dateTime: string;
}

export enum EventType {
    THRESHOLD = 'THRESHOLD', SCHEDULE = 'SCHEDULE'
}

export enum ActionType {
    CONTROL = 'CONTROL', AUTOMATION_GROUP = 'AUTOMATION_GROUP', AUTOMATION = 'AUTOMATION'
}
