import {jsonArrayMember, jsonMember, jsonObject} from 'typedjson';
import {Metric} from './Metric';

@jsonObject
export class MetricData {
    @jsonMember
    public metric: Metric;
    @jsonArrayMember(Number)
    public values: number[];
    @jsonArrayMember(String)
    public timestamps: string[];
    @jsonMember
    public startTime: string;
    @jsonMember
    public endTime: string;
}
