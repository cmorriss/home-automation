import {Action} from './Action';
import {Schedule} from './Schedule';
import {ActionType} from './Automation';

import 'reflect-metadata'
import {jsonMember, jsonObject} from 'typedjson';

@jsonObject({onDeserialized: 'init'})
export class ScheduleAction extends Action {
    @jsonMember
    public id: number;
    @jsonMember
    public schedule: Schedule;

    public name: string;
    public type: ActionType;

    public init() {
        this.name = 'Schedule ' + this.id;
        this.type = ActionType.SCHEDULE;
    }
}
