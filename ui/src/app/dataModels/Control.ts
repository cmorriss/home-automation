
import 'reflect-metadata'
import {jsonMember, jsonObject} from "typedjson";

@jsonObject
export class Control {
    @jsonMember
    public id: number;
    @jsonMember
    public givenId: string;
    @jsonMember
    public name: string;
    @jsonMember
    public type: ControlType;
    @jsonMember
    public state: ControlState;
    @jsonMember
    public lastUpdate: string;
}

export enum ControlType {
    IRRIGATION_VALVE = 'IRRIGATION_VALVE', LIGHT_SWITCH = 'LIGHT_SWITCH'
}

export enum ControlState {
    OFF = 'OFF', ON = 'ON'
}
