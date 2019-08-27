interface EnumIdentity { }
export class ScheduleStatusEnum implements EnumIdentity {

    private static AllValues: { [name: string] : ScheduleStatusEnum } = {};

    static readonly Loading = new ScheduleStatusEnum('loading', "Loading...", 'warning');
    static readonly Active = new ScheduleStatusEnum('active', "Active", 'success');
    static readonly Paused = new ScheduleStatusEnum('paused', "Paused", 'warning');
    static readonly Stopped = new ScheduleStatusEnum('stopped', "Stopped", 'danger');

    private constructor(
        public readonly value: string,
        public readonly displayValue: string,
        public readonly color: string
    ) {
        ScheduleStatusEnum.AllValues[value] = this;
    }

    public static parseEnum(data: string) : ScheduleStatusEnum {
        console.log("parsing schedule status for string" + data);
        return ScheduleStatusEnum.AllValues[data];
    }
}
