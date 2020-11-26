import {jsonMember, jsonObject} from 'typedjson';

@jsonObject
export class Metric {
    @jsonMember
    public id: number;
    @jsonMember
    public name: string;
    @jsonMember
    public externalName: string;
    @jsonMember
    public externalNamespace: string;
    @jsonMember
    public period: number;
    @jsonMember
    public statistic: string;
}
