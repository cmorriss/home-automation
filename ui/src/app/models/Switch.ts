import {Schedule} from './Schedule';

export class Switch {
    constructor(
        public id: number,
        public name: string,
        public type: string,
        public location: String,
        public locationId: number,
        public locationStatus: String,
        public locationStatusMessage: String,
        public on: boolean,
        public lastUpdate: number,
        public schedule: Schedule) {
    }
}
