import {ScheduleStatusEnum} from './ScheduleStatusEnum';

export class ScheduleStatusView {
    public isNotPaused: boolean;
    constructor(
        public status: ScheduleStatusEnum,
        public pausedUntilDate: Date
    ) {
        this.isNotPaused = status != ScheduleStatusEnum.Paused;
    }
}
