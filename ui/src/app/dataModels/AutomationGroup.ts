import {Automation} from './Automation';
import 'reflect-metadata';
import {jsonArrayMember, jsonMember, jsonObject} from 'typedjson';
import {AutomationStatusEnum} from '../viewModels/AutomationStatusEnum';

@jsonObject
export class AutomationGroup {
    @jsonMember
    public id: number;
    @jsonMember
    public name: string;
    @jsonMember(AutomationStatusEnum.serializer)
    public status: AutomationStatusEnum;
    @jsonMember
    public resumeDate: string;
    @jsonArrayMember(Automation)
    public items: Automation[];
}
