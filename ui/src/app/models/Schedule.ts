export class Schedule {
    constructor(
        public id: number,
        public daysOn: string[],
        public startTime: string,
        public duration: number) {
    }
}
