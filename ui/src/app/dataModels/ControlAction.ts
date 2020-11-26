import {Control, ControlState} from './Control';
import {Action} from "./Action";
import {ActionType} from "./Automation";

import 'reflect-metadata'
import {jsonMember, jsonObject} from "typedjson";

@jsonObject({onDeserialized: 'init'})
export class ControlAction extends Action {
    @jsonMember
    public id: number;
    @jsonMember
    public state: ControlState;
    @jsonMember
    public control: Control;

    public type: ActionType;
    public name: string;

    public init() {
        this.type = ActionType.CONTROL;
        this.name = this.control.name;
    }
}
