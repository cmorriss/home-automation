import 'reflect-metadata';
import { jsonObject, jsonMember, TypedJSON } from 'typedjson';
import {ActionType} from "./Automation";

@jsonObject
export class Action {
    @jsonMember
    public id: number;

    public type: ActionType;
    public name: string;
}