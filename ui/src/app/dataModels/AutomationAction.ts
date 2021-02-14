import {Action} from './Action';
import {ActionType, Automation} from './Automation';

import 'reflect-metadata'
import {jsonMember, jsonObject} from 'typedjson';

@jsonObject({onDeserialized: 'init'})
export class AutomationAction extends Action {
    @jsonMember
    public id: number;
    @jsonMember
    public automation: Automation;

    public name: string;
    public type: ActionType;

    public init() {
        this.name = 'Automation ' + this.id;
        this.type = ActionType.AUTOMATION;
    }
}
