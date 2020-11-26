import {Control} from './Control';

import 'reflect-metadata'
import {jsonArrayMember, jsonMember, jsonObject} from "typedjson";

@jsonObject
export class ControlGroup {
    @jsonMember
    public id: number;
    @jsonMember
    public name: string;
    @jsonArrayMember(Control)
    public items: Control[];
}
