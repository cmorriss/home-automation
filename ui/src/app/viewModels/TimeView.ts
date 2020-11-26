export class TimeView {
    constructor (
        time: string
    ) {
        this.setTime(time)
    }

    public hour: string;
    public minute: string;

    public setTime(time: string) {
        let timeSplit = time.split(':');
        this.hour = timeSplit[0];
        this.minute = timeSplit[1];
    }

    public getTime(): string {
        return this.hour + ":" + this.minute;
    }
}