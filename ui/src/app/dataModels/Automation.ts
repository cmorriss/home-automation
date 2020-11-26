import 'reflect-metadata';
import {jsonMember, jsonObject} from 'typedjson';
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
}

export enum EventType {
    SCHEDULE = 'SCHEDULE', THRESHOLD = 'THRESHOLD'
}

export enum ActionType {
    CONTROL = 'CONTROL', AUTOMATION_GROUP = 'AUTOMATION_GROUP', SCHEDULE = 'SCHEDULE'
}
