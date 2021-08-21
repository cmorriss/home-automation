import {jsonMember, jsonObject} from 'typedjson';
import {ControlType} from './Control';

@jsonObject
export class ControlDef {
    @jsonMember
    public id: number;
    @jsonMember
    public name: string;
    @jsonMember
    public type: ControlType;
}
