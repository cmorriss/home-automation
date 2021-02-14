
import 'reflect-metadata'
import {jsonMember, jsonObject} from "typedjson";
import {Event} from './Event';
import {EventType} from './Automation';

@jsonObject
export class Threshold implements Event {
    @jsonMember
    id: number;

    @jsonMember
    type: EventType;
}
