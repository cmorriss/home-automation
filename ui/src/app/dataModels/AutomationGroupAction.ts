import {Action} from "./Action";
import {ActionType} from "./Automation";
import {AutomationGroup} from "./AutomationGroup";
import {AutomationStatusEnum} from "../viewModels/AutomationStatusEnum";

import 'reflect-metadata'
import {jsonMember, jsonObject} from "typedjson";

@jsonObject({onDeserialized: 'init'})
export class AutomationGroupAction extends Action {
    @jsonMember
    public id: number;
    @jsonMember
    public automationGroup: AutomationGroup;
    @jsonMember(AutomationStatusEnum.serializer)
    public status: AutomationStatusEnum;
    @jsonMember
    public pausedUntil: string;

    public name: string;
    public type: ActionType;

    public init() {
        this.name = this.automationGroup.name;
        this.type = ActionType.AUTOMATION_GROUP;
    }
}