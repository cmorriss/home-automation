import {Schedule} from './Schedule';
import {Switch} from './Switch';

export class SwitchView {
    public static readonly offColor : string = "medium";
    public static readonly onColor : string = "success";
    public static readonly disconnectedColor : string = "danger";
    public static readonly errorColor : string = "warning";

    public buttonColor: string;
    public id: number;
    public name: string;
    public kind: string;
    public location: string;
    public locationId: number;
    public locationStatus: string;
    public locationStatusMessage: string;
    public on: boolean;
    public lastUpdate: number;
    public schedule: Schedule;

    constructor(aSwitch: Switch) {
        Object.assign(this, aSwitch);
        this.updateColor();
    }

    public toggle() {
        this.on = !this.on;
        this.updateColor();
    }

    private updateColor() {
        if (this.locationStatus == "DISCONNECTED") {
            this.buttonColor = SwitchView.disconnectedColor;
        } else if (this.locationStatus == "ERROR") {
            this.buttonColor = SwitchView.errorColor;
        } else {
            if (this.on) {
                this.buttonColor = SwitchView.onColor;
            } else {
                this.buttonColor = SwitchView.offColor;
            }
        }
    }

    public toSwitch(): Switch {
        return new Switch(
            this.id,
            this.name,
            this.kind,
            this.location,
            this.locationId,
            this.locationStatus,
            this.locationStatusMessage,
            this.on,
            this.lastUpdate,
            this.schedule
        );
    }
}
