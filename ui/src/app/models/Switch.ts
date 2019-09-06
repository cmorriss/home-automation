import {Schedule} from './Schedule';

export class Switch {
    constructor(
        public id: number,
        public name: string,
        public kind: string,
        public location: string,
        public locationId: number,
        public locationStatus: string,
        public locationStatusMessage: string,
        public on: boolean,
        public lastUpdate: number,
        public schedule: Schedule) {
    }
}
