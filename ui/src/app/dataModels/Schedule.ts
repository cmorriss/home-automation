import {Event} from "./Event";
import {EventType} from "./Automation";

import 'reflect-metadata'
import {jsonArrayMember, jsonMember, jsonObject} from "typedjson";

@jsonObject
export class Schedule implements Event {
    @jsonMember
    public id: number;
    @jsonArrayMember(String)
    public daysOfTheWeek: string[];
    @jsonMember
    public time: string;
    @jsonMember
    public dateTime: string;

    public type = EventType.SCHEDULE;
}
