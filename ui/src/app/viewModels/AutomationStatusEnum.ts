
export class AutomationStatusEnum {

    private static AllValues: { [name: string]: AutomationStatusEnum } = {};

    static readonly Loading = new AutomationStatusEnum('LOADING', 'Loading...', 'warning');
    static readonly Active = new AutomationStatusEnum('ACTIVE', 'Active', 'success');
    static readonly Paused = new AutomationStatusEnum('PAUSED', 'Paused', 'warning');
    static readonly Stopped = new AutomationStatusEnum('STOPPED', 'Stopped', 'danger');

    private constructor(
        public readonly value: string,
        public readonly displayValue: string,
        public readonly color: string
    ) {
        AutomationStatusEnum.AllValues[value] = this;
    }
    public readonly isNotPaused: boolean = this.value !== 'PAUSED';

    public static parseEnum(data: string): AutomationStatusEnum {
        console.log('parsing automation status for string ' + data);
        return AutomationStatusEnum.AllValues[data];
    }

    public static serializer = {
        serializer: enumValue => enumValue.value,
        deserializer: stringValue => AutomationStatusEnum.parseEnum(stringValue)
    }
}
