import {EventType} from './Automation';

import 'reflect-metadata'
import {jsonMember, jsonObject} from 'typedjson';

@jsonObject
export class Event {
    @jsonMember
    id: number;

    type: EventType;
}
